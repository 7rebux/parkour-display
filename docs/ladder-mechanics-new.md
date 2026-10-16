# Ladder mechanics in Minecraft 1.8.9

How ladders and vertical movement on them work, written as a reference for
building tools and mods.

**Scope.** Java Edition **1.8.9**. Every number here was reproduced by a
tick-by-tick simulation of the movement code, and the reference case was checked
against an in-game capture tick for tick. Nothing here is guaranteed for other
versions — the ordering inside a tick changed more than once in Minecraft's
history, so re-derive rather than assume.

The Java below is **simplified**: the control flow and the constants are real,
the surrounding branches (water, lava, flying, potion effects, horizontal
friction) are cut.

---

## 1. Constants

| Name | Value | Where |
| --- | --- | --- |
| Gravity | `0.08` per tick | `motionY -= 0.08` at the end of `moveEntityWithHeading` |
| Drag | `0.98` per tick | `motionY *= 0.98` immediately after gravity |
| Jump impulse | `0.42` | `EntityLivingBase.jump()`, `+0.1` per Jump Boost level |
| Climb velocity | `0.2` | written when `isCollidedHorizontally && isOnLadder()` |
| Ladder speed limit | `0.15` | fall floor on Y, and a `±0.15` clamp on X and Z |
| Velocity cutoff | `0.005` | `onLivingUpdate`, per axis, before anything else |
| Ladder bounding box | `2/16 × 1 × 1` (0.125 deep) | vines have no box at all |

Derived numbers that show up constantly — memorise these, most reasoning about
ladders is done in them:

| Quantity | Value | Where it comes from |
| --- | --- | --- |
| Standing velocity | `−0.0784` | `(0 − 0.08) × 0.98`, what a player on the ground carries |
| Climb speed | **`0.1176`** blocks/tick | `(0.2 − 0.08) × 0.98` — gravity still runs after the overwrite |
| Carry-over tick | **`+0.0368`** | `(0.1176 − 0.08) × 0.98` — one more upward tick after the last climb tick |
| First fall tick | `−0.0423` | `(0.0368 − 0.08) × 0.98` — the cheapest possible overshoot |
| Terminal velocity | `−3.92` (`−3.9188` by tick 400) | `0.02V = −0.0784` |
| Ticks per block climbed | `1 / 0.1176 = 8.503` | so a ladder costs 8 or 9 ticks, alternating |
| A ladder slide | `0.15` blocks/tick | the fall cap, ~26× slower than terminal velocity |

**Float precision.** The clean decimals above are what the arithmetic in this
document uses. Vanilla stores several of them as float-widened doubles
(`0.41999998688697815` for the jump, `0.9800000190734863` for drag,
`0.15000000596046448` for the ladder limit). That divergence is invisible at
four decimal places but real in the last bits. Code that has to agree with the
server bit-for-bit should take the constants out of the mappings it compiles
against, not out of this table.

---

## 2. The tick, in order

This is the whole model. Everything else here is a consequence of the *ordering*,
especially of which side of the move a velocity is written on.

```java
// EntityLivingBase.onLivingUpdate() — simplified
public void onLivingUpdate() {
    if (Math.abs(this.motionY) < 0.005) {      // 1. cutoff, done per axis
        this.motionY = 0.0;
    }

    this.updateEntityActionState();            // 2. read inputs (isJumping, sneak, …)

    if (this.isJumping && this.onGround) {     // 3. jump — pays out this tick
        this.jump();                           //    motionY = 0.42
    }

    this.moveEntityWithHeading(this.moveStrafing, this.moveForward);
}

// EntityLivingBase.moveEntityWithHeading() — simplified
public void moveEntityWithHeading(float strafe, float forward) {
    if (this.isOnLadder()) {                   // 4. three guards, BEFORE the move
        float limit = 0.15F;
        this.motionX = MathHelper.clamp(this.motionX, -limit, limit);
        this.motionZ = MathHelper.clamp(this.motionZ, -limit, limit);
        this.fallDistance = 0.0F;              //    why sliding never hurts

        if (this.motionY < -0.15) {            //    fall cap
            this.motionY = -0.15;
        }

        if (this.isSneaking() && this.motionY < 0.0) {   // downward motion only
            this.motionY = 0.0;
        }
    }

    // 5. the move. Sets onGround on a landing and isCollidedHorizontally
    //    when the horizontal move was blocked.
    this.moveEntity(this.motionX, this.motionY, this.motionZ);

    if (this.isCollidedHorizontally && this.isOnLadder()) {  // 6. climb, AFTER the move
        this.motionY = 0.2;
    }

    this.motionY -= 0.08;                      // 7. gravity, then drag
    this.motionY *= 0.98F;
}
```

Four ordering facts fall out, and all four are load-bearing:

1. **The jump pays out on the tick it is pressed.** It is written before the
   move, so the player rises 0.42 on that same tick.
2. **The climb pays out one tick late.** It is written after the move, so climb
   speed is what the *next* tick moves at. The last tick tagged `onLadder` still
   moves at whatever speed it already had.
3. **The fall cap pays out one tick early.** It is read before the move, off the
   block the feet *start* the tick in — so it can bind on a tick that ends with
   the player already out of the ladder block and carrying no `onLadder` flag.
4. **Gravity runs after everything, including after a landing.** `onGround`
   zeroes the velocity inside `moveEntity`, then gravity and drag still run, so
   a player standing still sits at `−0.0784` forever, not at 0.

Consequence for any simulation: **a scenario without a jump must start tick 1
carrying `−0.0784`, not `0`.** Seeding a zero puts every subsequent tick one
behind.

---

## 3. Being on a ladder

```java
public boolean isOnLadder() {
    int x = MathHelper.floor(this.posX);
    int y = MathHelper.floor(this.getEntityBoundingBox().minY);   // the feet
    int z = MathHelper.floor(this.posZ);
    Block block = this.worldObj.getBlockState(new BlockPos(x, y, z)).getBlock();
    return block == Blocks.ladder || block == Blocks.vine;
}
```

- **One block, at the feet.** The lowest point of the bounding box, floored.
  What the upper half of the player is next to makes no difference. The wiki's
  "the player's lower half is in the block occupied by a ladder" is exactly this
  check.
- **Vines count too**, for all of it — climb, cap, clamp, sneak. Vines just have
  no collision box, so you can stand inside one without being pushed out.
- Vanilla additionally excludes spectators, and the sneak guard is player-only
  (`this.isSneaking() && this instanceof EntityPlayer`). Neither matters for
  movement maths, both matter if you are hooking the method.
- **Two different volumes are in play** and it is easy to conflate them: the
  ladder's *bounding box* (2/16 deep, what stops you and what makes you
  `isCollidedHorizontally`) and the *block* the check above reads (the full
  1×1×1 cell). You can be inside the block without touching the box.

Because the check floors the feet, **ladder contact starts on the tick Y crosses
into the block and ends on the tick it crosses out**. On a jump from y = 0 into
a ladder at block 1, that is tick 3 (`y = 1.0013`). From a **slab** (y = 0.5) it
is tick 2 — half a block of head start moves every downstream number, which is
why slab setups have their own optimal inputs.

---

## 4. The four things a ladder does

| Effect | Needs | Value | Side of the move |
| --- | --- | --- | --- |
| Horizontal clamp | `isOnLadder()` | X and Z clamped to `±0.15` | before |
| Fall cap | `isOnLadder()` | `motionY` floored at `−0.15` | before |
| Sneak hold | `isOnLadder()` + sneaking + `motionY < 0` | `motionY = 0` | before |
| Climb | `isOnLadder()` + `isCollidedHorizontally` | `motionY = 0.2` | **after** |

Only the climb needs a key. The other three fire on presence alone — a player
falling past a ladder they never touch a key on is still slowed to 0.15 a tick.

### Climbing

`isCollidedHorizontally` is set by `moveEntity` when the horizontal move was
blocked. So climbing is not "hold W at a ladder", it is **"be inside the ladder
block while pushing into something solid"**:

- The wall does not have to be the ladder. The block it hangs on, or any other
  block you run into, works identically.
- Holding a key with nothing to collide against does nothing.
- Being in the block without pushing does nothing — you fall, capped at 0.15.

A held climb moves **0.1176** per tick, not 0.2, because gravity and drag run
after the overwrite.

### Sneaking

`flag && this.motionY < 0.0D` — **downward motion only**. Consequences:

- Sneaking cannot cancel a climb, so climbing and sneaking coexist fine.
- Sneaking **cannot trim an overshoot** at the top of a stack, which is the
  obvious thing to want from it. By the time a climb overshoots, the feet are
  already above the ladder block and the guard is not even reached.
- Held on every tick of the optimal 13 t, 9 t and 39 t ascents below, it fires
  **zero times**. An exhaustive 2¹² × 2¹² sweep of press × sneak patterns on the
  reference ladder still bottoms out at 13 t.

Sneaking is a **descent** tool: it pins you to the ladder at your current height.

### Fall damage

`fallDistance = 0.0F` is set inside the same `isOnLadder()` block, every tick.
That is why sliding down a ladder is free of damage, and why leaving the ladder
to fall is not.

---

## 5. Reference traces

The tables show Δ Y, not velocity. They are the same number on every tick except
a landing, where the collision truncates the move — and note that the velocity
after a landing is not zero either (§2.4). **A 0.0000 in a Δ Y column on a
landing tick is a bug in the simulation producing it.**

### Jump into a ladder, W held from tick 1 — 15 ticks

World: player on y = 0, single ladder at block 1, top of its box at y = 2.

```
 tick        Y       Δ Y   state
    0   0.0000    0.0000   onGround
    1   0.4200    0.4200   collidedHorizontally
    2   0.7532    0.3332   collidedHorizontally
    3   1.0013    0.2481   onLadder collidedHorizontally   ← contact
    4   1.1189    0.1176   onLadder collidedHorizontally   ← climb speed starts
    …
   11   1.9421    0.1176   onLadder collidedHorizontally   ← last onLadder tick
   12   2.0597    0.1176   collidedHorizontally            ← climb pays out late
   13   2.0966    0.0368   collidedHorizontally            ← carry-over
   14   2.0543   -0.0423   collidedHorizontally
   15   2.0000   -0.0543   onGround
```

### The same ladder, W held 4–10 — 13 ticks

Both tricks (§7) applied: start pushing three ticks later, stop one tick early.

```
 tick        Y       Δ Y   state
    0   0.0000    0.0000   onGround
    1   0.4200    0.4200
    2   0.7532    0.3332
    3   1.0013    0.2481   onLadder
    4   1.1661    0.1648   onLadder collidedHorizontally   ← keeps the jump's own 0.1648
    5   1.2837    0.1176   onLadder collidedHorizontally
    …
   10   1.8717    0.1176   onLadder collidedHorizontally
   11   1.9893    0.1176   onLadder                        ← climbs anyway, key already released
   12   2.0262    0.0368                                   ← carry-over clears the top
   13   2.0000   -0.0262   onGround
```

Every tick from 4 on sits **0.0472 higher** than in the plain run
(`0.1648 − 0.1176`), and the overshoot at the top is 0.0262 instead of 0.0966 —
small enough to be given back in a single fall tick.

### Walk-up climb, no jump — 12 ticks of airtime

World: ladder at the player's own block, top of the block above at y = 1.

```
 tick        Y       Δ Y   state
    0   0.0000    0.0000   onGround onLadder
    1   0.0000    0.0000   onGround onLadder collidedHorizontally   ← still resting
    2   0.1176    0.1176   onLadder collidedHorizontally
    …
   10   1.0584    0.1176   collidedHorizontally
   11   1.0952    0.0368   collidedHorizontally
   12   1.0530   -0.0423   collidedHorizontally
   13   1.0000   -0.0530   onGround
```

Tick 1 does not move the player at all — the climb is written *after* that
tick's move — so airtime is measured from tick 1, not tick 0: **12 ticks**, the
same as an ordinary jump, ending a block higher.

### Airtime of the basic movements

| Movement | Leaves | Lands | Airtime | Net Y |
| --- | --- | --- | --- | --- |
| ordinary jump | after tick 0 | tick 12 | 12 t (0.60 s) | 0, peak 1.2492 |
| walk off a 1-block ledge | after tick 0 | tick 5 | 5 t | −1 |
| walk-up climb | after tick **1** | tick 13 | 12 t | +1 |
| jump into a ladder, W held | after tick 0 | tick 15 | 15 t | +2 |
| jump past a ladder, no keys | after tick 0 | tick 12 | 12 t | 0, peak 1.2492 |

A plain jump's apex is **1.2492**, held across ticks 5 *and* 6 because the
0.005 cutoff zeroes tick 6's 0.0030. Without the cutoff it would be 1.2522 — if
that number appears, the cutoff is missing.

---

## 6. Measuring, for a tracker

**Airtime** is measured **between the two resting ticks**: the last tick still on
a surface and the first tick back on one. In a ladder trace that is literally
the gap between the two `onGround` tags. Measuring from the first tick that
*moves* is off by one on any ascent that begins with a stationary tick (the
walk-up climb above).

Flags worth sampling, per tick, at end of tick:

| Flag | Meaning | How to get it |
| --- | --- | --- |
| `onGround` | resting on a surface, did not rise this tick | `entity.onGround` |
| `onLadder` | `isOnLadder()` — feet block only, nothing about keys | call it |
| `isCollidedHorizontally` | horizontal move was blocked | `entity.isCollidedHorizontally` |
| climbing | `onLadder && isCollidedHorizontally` | the two above |

Sample **after** the movement tick has run (a post-hook on the living update, or
the end of `onLivingUpdate`), and store `posY` and `motionY` together — they are
only meaningful as a pair.

Recipes:

- **Ticks spent on a ladder** — count ticks where `isOnLadder()` was true at end
  of tick. Note this counts one tick *fewer* than the ticks that moved at climb
  speed, because of the one-tick-late payout.
- **Height the climb started at** — the `posY` on the first tick where
  `onLadder && isCollidedHorizontally` both held. The player does not actually
  move at climb speed until the tick after that.
- **The climb ended here** — last tick where both held. The tick after it still
  moves 0.1176; the tick after *that* moves +0.0368.
- **Overshoot at the top** — `apexY − surfaceY`. Under `0.0423` it costs no
  extra fall tick; above it, each additional fall tick is charged back.
- **Was this a capped fall or a free fall** — `Δ Y == −0.15` exactly means the
  cap bound. Compare against `isOnLadder()` at the *start* of the tick, not the
  end.

---

## 7. Input tricks

Three inputs change the outcome, all about *when* you push into the ladder
rather than how hard. None of them changes climb speed — the climb is a flat
0.1176 and there is no faster sustained ascent.

### 7.1 Delay the press ("bank height")

Do not press into the ladder on the tick the feet arrive. Wait, and that tick
keeps the **jump's own** velocity instead of being overwritten with climb speed.
On the reference ladder tick 4 moves 0.1648 rather than 0.1176, so everything
above it sits **0.0472 higher**.

The optimum is **one tick after contact**, not "tick 4" — from the ground that
is tick 4, from a slab it is tick 3. Later than that costs: from tick 5 on, the
decaying jump is already *slower* than 0.1176 and waiting is pure loss.

Banked height is not itself a saving. It pays **only when it crosses a block
boundary**, letting the climb end a tick earlier. When it does not, it is pure
overshoot that has to be given back on the way down — a two-ladder stack loses a
tick to it (22 t held, 23 t delayed).

### 7.2 Leave a tick early ("spend the carry-over")

After the last climb tick the velocity is still 0.1176, so the formula produces
**one more upward tick of +0.0368** for free. Break the horizontal collision one
tick before the top — strafe off, or steer away — and that carry-over coasts you
the rest of the way, saving the last climb tick and usually a fall tick after the
apex.

Alone it usually fails: on the reference ladder it tops out at
`1.9421 + 0.0368 = 1.9789` against a surface at 2 and slides back down. It needs
the delay's banked height under it.

### 7.3 Release one tick mid-climb ("trim the overshoot")

Let go for a **single tick** in the middle of the climb and the next tick moves
`(0.1176 − 0.08) × 0.98 = 0.0368` instead of 0.1176 — everything above it sits
**0.0808 lower**. Let go for two ticks and the second pays out −0.0423, a fall,
so it is a one-tick move only.

It buys nothing on the way up; it is a **trim on the overshoot at the top**, for
setups that would otherwise clear a surface by more than 0.0423 and pay a fall
tick for it. The released tick can sit **anywhere** in the climb — only how many
released ticks there are matters, because the effect is a flat offset on
everything after.

It **only** pays as the third of three, and only on a **slab start with an even
number of ladders** (19 → 18, 36 → 35, 53 → 52 for 2, 4, 6 ladders). From flat
ground it never helps in any combination.

### The reference ladder, all four combinations

Player at y = 0, one ladder at block 1, landing on top of its box at y = 2:

| W held | Lands |
| --- | --- |
| 1 – 11 (plain) | 15 |
| 4 – 11 (delay only) | 15 — banks height, spends nothing |
| 1 – 10 (early leave only) | **never** — 1.9789, short of 2, slides back down |
| **4 – 10 (both)** | **13** |

That is the "13t ladder" of speedrunning: one climb tick saved and one fall tick
saved.

---

## 8. Optimal inputs, swept

Jump on tick 1 in every row. Airtime measured as in §6. Verified two ways: a
sweep of every contiguous press window, and an exhaustive breadth-first search
over *arbitrary* press patterns (§9).

### From flat ground onto a stack starting at block 1

| Ladders | Jump + hold W | Best | Optimal input |
| --- | --- | --- | --- |
| 1 | 15 | **13** | W 4–10 (delay + early leave) |
| 2 | 22 | **22** | hold from 1 — nothing helps |
| 3 | 32 | **30** | W 4–27 |
| 4 | 41 | **39** | W 1–36 (early leave alone) |
| 5 | 49 | **47** | W 4–44 |
| 6 | 58 | **56** | W 1–53 |

- The **early leave is nearly always right**, and always worth exactly 2 ticks.
  Two ladders is the sole exception.
- The **delay only pays on an odd number of ladders**; on an even number it
  costs a tick. Same cause as the two-ladder case — whether the banked 0.0472
  crosses a block boundary alternates with each ladder added.
- **A ladder costs 8.5 ticks**, alternating +9, +8, +9.

### From a slab (start y = 0.5)

| Ladders | Jump + hold W | Best |
| --- | --- | --- |
| 1 | 12 | **9** |
| 2 | 20 | **18** |
| 3 | 29 | **26** |
| 4 | 37 | **35** |
| 5 | 46 | **43** |
| 6 | 54 | **52** |

The slab's feet reach the ladder on **tick 2**, so the delay that wins is `W
from 3`. Odd stacks want delay + early leave (`W 3–6` gives 9 t on one ladder);
even stacks additionally want the mid-climb release (`W 4–15` with tick 10
released gives 18 t on two, against 19 t without it).

### Ladder starting at the player's feet — jump at the base

Whenever the player is `onGround` at the foot of a ladder, `jump()` is legal and
a jump gives 0.42 in one tick against the climb's 0.1176. This is worth far more
than either input trick:

| Ladders | Walk in and hold W | Best with a jump | Saved |
| --- | --- | --- | --- |
| 1 | 12 | **6** | 6 |
| 2 | 21 | **13** | 8 |
| 3 | 29 | **22** | 7 |
| 4 | 38 | **30** | 8 |
| 5 | 46 | **39** | 7 |

### Descending

The −0.15 cap needs `isOnLadder()`, so stepping out of the block frees the fall
to accelerate toward −3.92:

| Drop | Sliding down the ladder | Free fall |
| --- | --- | --- |
| 3 blocks | 21 t | 9 t |
| 5 blocks | 34 t | 12 t |
| 10 blocks | 68 t | 17 t |
| 20 blocks | 134 t | 24 t |

The catch is `fallDistance = 0` (§4): sliding is damage-free, dropping is not
above three blocks.

---

## 9. Algorithms

### Simulating

One tick, as a pure function. `pressing` is `isCollidedHorizontally` for that
tick, which a tracker reads off the entity and a planner has to supply as input —
it depends on X/Z geometry and on which keys are held, and cannot be derived
from Y alone.

```
step(y, v, tick, jumping, pressing, sneaking):
    if abs(v) < 0.005:            v = 0
    if jumping and onGround(y):   v = 0.42

    if onLadder(y):                          # guards read the block the feet START in
        if v < -0.15:                        v = -0.15
        if sneaking and v < 0:               v = 0

    prevY = y
    next  = y + v
    if v < 0 and next would cross a surface at s <= y:
        next = s; v = 0                      # landing cancels the rest of the move

    y = next
    if onLadder(y) and pressing:  v = 0.2    # AFTER the move
    v = (v - 0.08) * 0.98                    # gravity, then drag

    return y, v, delta = y - prevY,
           onGround = restingOnSurface(y) and y <= prevY,
           onLadder = onLadder(y)
```

- Seed tick 1 with `v = -0.0784` when there is no jump (§2).
- `onLadder(y)` is `floor(y + 1e-9) in [ladderFrom, ladderTo]`. **The epsilon is
  required**: floating point leaves Y a hair under a whole block and flooring
  without it silently loses a tick of contact.
- The landing test needs the same epsilon (`surface <= y + EPS`).

### Finding the optimal input

The search space is which ticks the player is pushing into the ladder (plus jump
on or off). Two approaches:

**Contiguous window.** Try every `(from, to)` window — about n² simulations,
instant. **It is not complete**: it cannot find the mid-climb release of §7.3,
so it reports 19 t for slab + 2 ladders where the true optimum is 18 t. It is
right on every setup where that release does not pay, which is every flat-ground
setup.

**Full BFS over arbitrary patterns** — complete, and still fast because the
state space collapses:

```
states = { (startY, -0.0784) }
for tick in 1..maxTicks:
    next = {}
    for (y, v) in states:
        for pressing in [true, false]:
            r = step(y, v, tick, …)
            if r.onGround and r.y == target:   record airtime = tick; done
            next[key(r.y, r.v)] = r            # dedup — this is what makes it cheap
    states = next
```

Dedup on the exact `(y, v)` pair (stringify at ~9 decimals). Ten ladders
searched exhaustively runs in seconds. Include sneak as a third branch only if
you want to prove it does nothing — it never wins on an ascent.

Score on **airtime**, not on height. Height is a trap: the highest apex is often
the *slowest* run, because clearing the top by more than 0.0423 costs a whole
extra fall tick. On a slab, pressing on tick 3 reaches 2.0086, on tick 4 reaches
2.0558, on tick 5 reaches 2.0212 — and 3 and 5 land on tick 9 while the highest,
4, lands on 10.

### Canonicalising a result

Many press windows produce identical flights: pressing before the feet reach the
ladder does nothing, and pressing after they leave the top does nothing. Collapse
outcomes by `(jump, airtime, landsOn, apex)` before showing them, and report the
tolerated window rather than one arbitrary member of it — otherwise a tool tells
the user to press on tick 1 when ticks 1–3 are equally fine.

---

## 10. Gotchas

- **Never seed a no-jump scenario with velocity 0.** It must be −0.0784.
- **The climb is one tick late, the cap is one tick early.** A tick tagged
  `onLadder` is not a tick that moved at climb speed, and a tick that was capped
  may carry no `onLadder` tag.
- **Δ Y ≠ velocity on a landing tick**, and the velocity after a landing is not
  zero.
- **Floor with an epsilon**, both for the ladder block and for the landing test.
- **Any wall counts** for `isCollidedHorizontally`, not just the ladder.
- **Letting go of W and strafing out of the block are different things.** Both
  end the climb, but only the second drops `isOnLadder()` — and with it the fall
  cap, the clamp and the `fallDistance` reset. A Y-only simulation cannot tell
  them apart, so a scenario has to say which one it means.
- **Sneak is downward-only and player-only**, and useless on any ascent.
- **Half-block starts change everything.** A slab moves the contact tick, and
  every optimal input with it.
- **Jump Boost** adds 0.1 per level to the impulse, so any "every jump" claim
  needs a hedge.
- Vines behave identically for movement but have no collision box, so the wall
  needed for `isCollidedHorizontally` has to come from somewhere else.

---

## 11. Not covered here

- **X and Z entirely.** Everything above is one-dimensional; horizontal geometry
  enters only as the `isCollidedHorizontally` tick ranges it produces. Anything
  about strafing out of a ladder column, ladder placement against corners, or
  which wall is actually being hit is outside it.
- **Water, lava, flying, elytra, slime blocks, jump-boost stacking.**
- **Server/client divergence.** All of this is one authoritative movement tick;
  reading it client-side sees the local simulation, which the server can correct.
- Whether any of it holds in versions other than 1.8.9.
