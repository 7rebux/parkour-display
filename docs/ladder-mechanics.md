# Ladder mechanics — Minecraft Java Edition 1.8.9

A self-contained reference for how ladders (and vines) affect **vertical**
movement in 1.8.9, down to the tick. Every number here comes from a tick
simulation that reproduces in-game captures exactly, and every optimum from an
exhaustive search over input sequences.

Code is MCP-named 1.8.9 (`EntityLivingBase`, `EntityPlayer`).

---

## 1. Base vertical physics

20 ticks per second. Each tick, after the move:

```
motionY = (motionY - 0.08) * 0.98
```

- **Jump impulse:** `jump()` sets `motionY = 0.42`.
- **Velocity cutoff:** at the start of `onLivingUpdate`, `|motionY| < 0.005` is
  snapped to `0`.
- **Terminal velocity:** `0.02·V = −0.0784` → **V = −3.92** blocks/tick.
- **Standing still:** landing zeroes `motionY` inside `moveEntity`
  (`Block.onLanded()`), but gravity and drag run afterwards, so a player on the
  ground always carries **−0.0784** into the next tick, never 0.

### Order inside one tick

```
onLivingUpdate():
  if |motionY| < 0.005: motionY = 0          // cutoff
  if jumping && onGround: jump()              // motionY = 0.42
  moveEntityWithHeading(strafe, forward):
    if isOnLadder():                          // ladder guards, BEFORE the move
      clamp motionX, motionZ to ±0.15
      fallDistance = 0
      if motionY < -0.15: motionY = -0.15     // fall cap
      if sneaking && motionY < 0: motionY = 0 // sneak pin
    moveEntity(motionX, motionY, motionZ)     // collisions, sets isCollidedHorizontally
    if isCollidedHorizontally && isOnLadder():
      motionY = 0.2                           // climb, AFTER the move
    motionY = (motionY - 0.08) * 0.98         // gravity + drag
```

The real code:

```java
// EntityLivingBase.moveEntityWithHeading, non-liquid branch (abridged)
if (this.isOnLadder()) {
    float f6 = 0.15F;
    this.motionX = MathHelper.clamp_double(this.motionX, (double)(-f6), (double)f6);
    this.motionZ = MathHelper.clamp_double(this.motionZ, (double)(-f6), (double)f6);
    this.fallDistance = 0.0F;

    if (this.motionY < -0.15D) {
        this.motionY = -0.15D;
    }

    boolean flag = this.isSneaking() && this instanceof EntityPlayer;

    if (flag && this.motionY < 0.0D) {
        this.motionY = 0.0D;
    }
}

this.moveEntity(this.motionX, this.motionY, this.motionZ);

if (this.isCollidedHorizontally && this.isOnLadder()) {
    this.motionY = 0.2D;
}

// ...
this.motionY -= 0.08D;
this.motionY *= 0.9800000190734863D;
```

### Reference airtimes (no ladder)

| Movement | Airtime | Net Y |
| --- | --- | --- |
| Jump on flat ground | **12 t** | 0, apex **1.2492** (held ticks 5 and 6) |
| Walk off a 1-block ledge | **5 t** | −1 |

Apex sanity check: **1.2492** with the cutoff, 1.2522 without it.

---

## 2. When are you "on a ladder"?

```java
public boolean isOnLadder() {
    int i = MathHelper.floor_double(this.posX);
    int j = MathHelper.floor_double(this.getEntityBoundingBox().minY);
    int k = MathHelper.floor_double(this.posZ);
    Block block = this.worldObj.getBlockState(new BlockPos(i, j, k)).getBlock();
    return (block == Blocks.ladder || block == Blocks.vine)
        && (!(this instanceof EntityPlayer) || !((EntityPlayer) this).isSpectator());
}
```

- It is the **block** at the player's position that counts — not the ladder's
  thin model.
- Horizontally: `posX`/`posZ` (the player's centre) must be in the ladder's
  block column. Equivalently, the player's 0.6-wide bounding box must reach into
  the block shrunk by 0.3 on X and Z.
- Vertically: the **bottom** of the bounding box (`minY`, the feet), **floored**.
  Contact begins the tick feet Y crosses a whole block into the ladder and ends
  the tick it crosses out of the top one. Not "lower half", not eye height.
- Ladder collision box: 2/16 × 1 × 1 against the wall. Vines have **no**
  collision box.

`isOnLadder()` alone — no key — is enough for the fall cap, the sneak pin and
the horizontal clamp. **Climbing** additionally needs `isCollidedHorizontally`.

---

## 3. What a ladder does

| Effect | Condition | When in the tick | Value |
| --- | --- | --- | --- |
| Horizontal clamp | `isOnLadder()` | before move | `motionX/Z` ∈ [−0.15, 0.15] |
| Fall cap | `isOnLadder()` | before move | `motionY ≥ −0.15` |
| Sneak pin | `isOnLadder()` + sneaking + `motionY < 0` | before move | `motionY = 0` |
| Climb | `isOnLadder()` + `isCollidedHorizontally` | **after** move | `motionY = 0.2` |
| No fall damage | `isOnLadder()` | before move | `fallDistance = 0` |

### Climb speed is 0.1176, not 0.2

The `0.2` is written after the move, then gravity and drag run on it:
`(0.2 − 0.08) × 0.98 = `**`0.1176`** blocks per tick. That is what the next
tick moves. One block of climbing ≈ `1 / 0.1176 = 8.503` ticks, so each extra
ladder in a stack costs alternately **9, 8, 9, 8…** ticks.

### `isCollidedHorizontally`, not "holding W"

Climbing needs you to be pushed into *any* wall while your feet are in the
ladder block. Any key that pushes you into a wall works — in a 1×1 shaft every
direction climbs. Releasing W while staying against the ladder stops the climb
but keeps `isOnLadder()`, so you slide down at the −0.15 cap. Strafing out of
the column drops `isOnLadder()` entirely and you free-fall.

### Timing asymmetries (real, not bugs)

- **The climb pays out one tick late.** It is written after the move, so the
  *next* tick moves 0.1176. The last tick you are `onLadder` still moves at
  whatever speed it had; the tick after it moves at climb speed.
- **The fall cap pays out one tick early (from the table's point of view).** It
  is checked before the move, against the block the feet *start* the tick in.
- **A jump pays out on the tick it is pressed** (it runs before
  `moveEntityWithHeading`), while a climb pays out the tick after.
- **A walk-up climb starts a tick late:** on tick 1 the player is still on the
  ground at the end of the move; the climb velocity only moves them on tick 2.

### Sneaking

Only cancels **downward** motion. It cannot cancel a climb or trim an
overshoot at the top — once the climb overshoots, the feet are above the ladder
block and the guard is not reached. Exhaustive sweeps of press × sneak patterns
never find a faster ascent with sneak. It is a descent / hold-position tool.

### Descending

Sliding is capped at 0.15/tick and resets `fallDistance`. Leaving the ladder
column lets you accelerate toward −3.92: ten blocks take **68 t** sliding vs
**17 t** falling — but falling takes damage beyond 3 blocks.

---

## 4. Ascent tricks

Airtime = ticks between the last tick resting on a surface and the first tick
resting again (top of the ladder's block counts as a surface: you land on the
collision box at the top).

### 4.1 Delay the climb ("pessi")

Parkour terms: a **jam** is jump + W on the same tick; **pessi Nt** jumps and
presses W N ticks later (pessi 3t → W on tick 4).

The jump's own ΔY per tick is `0.42, 0.3332, 0.2481, 0.1648, 0.0831, …`. While
the jump moves faster than the 0.1176 climb, letting it run banks height. Tick 4
(0.1648) is the last tick that beats the climb; from tick 5 on, waiting costs.

General rule: **delay to one tick after the feet reach the ladder block.** From
the ground with the ladder one block up, contact is tick 3 → W on tick 4. From a
slab, contact is tick 2 → W on tick 3. With the ladder at the feet, pessi 1t.

Banked height only helps if it **crosses a block boundary** so the climb ends a
tick earlier. Otherwise the overshoot is charged back on the way down — the
first tick of a fall covers only **0.0423**, so clearing the top by more than
that costs an extra landing tick. *Height is not the objective — airtime is.
Among inputs with the same airtime, the one with the biggest overshoot (highest
peak above the top) is best: it clears with the most margin.*

### 4.2 Early exit

Stop pushing into the ladder on the last climb tick. That tick still moves
0.1176 (paid by the previous tick), but its velocity is now
`(0.1176 − 0.08) × 0.98 = `**`0.0368`** instead of a fresh 0.1176. If that
coast still clears the top, you skip one climb tick and one fall tick.

```
playerY + 0.0368 >= ladderMaxY
```

where `playerY` is the feet Y one tick before the last regular climb tick
and `ladderMaxY` is the top of the stack. **Whenever it works it saves exactly
2 ticks.** If it falls short you slide back down at the cap (or free-fall if you
left the column).

### 4.3 Mid-climb release

Release for exactly **one** tick mid-climb. The following tick moves 0.0368
instead of 0.1176, so everything after sits **0.0808 lower** — a flat phase
shift against the block grid. Its position in the climb doesn't matter, only the
count. Two ticks released is a fall (−0.0423) and never helps; across every
swept setup one release is the most that ever pays.

It does nothing alone and nothing with only the delay. It pays **only as the
third of three** (delay + release + early exit), and only from a **raised
start** (roughly 2/16 to 9/16 of a block: slabs, snow layers, trapdoors,
daylight sensors, beds). From flat ground it never helps.

---

## 5. Worked results

Ladder stack starting one block up, jumping from y = 0:

| Setup | Plain (jam) | Best | Input |
| --- | --- | --- | --- |
| 1 ladder | 15 | **13** | pessi 3t + early exit (W 4–10) |
| 2 ladders | 22 | **22** | nothing helps |
| 4 ladders | 41 | **39** | jam + early exit (pessi 3t alone: 40) |
| Slab + 2 ladders above | 20 | **18** | pessi 3t + one-tick release + early exit |

Reference ladder (1 ladder, block 1, surfaces at 0 and 2) in detail:

| W held | Lands |
| --- | --- |
| 1–11 (plain) | 15 |
| 4–11 (delay only) | 15 |
| 1–10 (early exit only) | never — 1.9421 + 0.0368 = 1.9789 < 2, slides back |
| **4–10 (both)** | **13** — tick 11 at 1.9893, tick 12 coasts to 2.0262, tick 13 lands |

Four stacked ladders: the jam sits at **4.9997** on tick 37, three
ten-thousandths short → lands on 41. Early exit there: 4.9997 + 0.0368 clears →
**39**.

1–10 ladders from y = 0, ladder starting at block 1:

| Ladders | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Plain | 15 | 22 | 32 | 41 | 49 | 58 | 66 | 75 | 83 | 92 |
| Best | 13 | 22 | 30 | 39 | 47 | 56 | 64 | 73 | 81 | 90 |

Odd counts: delay to tick 4 + early exit. Even counts: hold from tick 1 +
early exit (the delay costs a tick on even stacks). Two is the lone exception
where nothing helps.

### Jump at the foot of a ladder

If the ladder starts at your feet and you're on the ground, jumping is legal and
gives 0.42 in one tick against the climb's 0.1176 — worth far more than the
other tricks:

| Ladders at feet | Walk in + hold W | Best with jump |
| --- | --- | --- |
| 1 | 12 | **6** (jump tick 1, W 2–3: pessi 1t + early exit) |
| 2 | 21 | **13** |
| 3 | 29 | **22** |
| 4 | 38 | **30** |
| 5 | 46 | **39** |

There is **no closed form**: every answer depends on where the 0.1176 steps fall
against whole blocks, since `isOnLadder()` floors Y. Simulate per setup.

---

## 6. Reference simulation

A 1-D sim that reproduces in-game captures tick for tick. It has no X/Z, so
`isCollidedHorizontally` and sneaking are inputs.

```js
// startY: feet Y; ladder blocks ladderFrom..ladderTo; surfaces: Y values you can land on.
// jumpOnTick1: whether jump() fires on tick 1. pressing(t), sneaking(t): per-tick inputs.
function simulate({ startY, ladderFrom, ladderTo, surfaces, jumpOnTick1, pressing, sneaking, lastTick }) {
  const EPS = 1e-9;
  const onLadderAt = (y) => {
    const b = Math.floor(y + EPS);
    return b >= ladderFrom && b <= ladderTo;
  };
  let y = startY;
  let v = (0 - 0.08) * 0.98; // a standing player carries -0.0784, not 0
  const rows = [{ tick: 0, y }];

  for (let t = 1; t <= lastTick; t++) {
    if (Math.abs(v) < 0.005) v = 0;      // cutoff
    if (t === 1 && jumpOnTick1) v = 0.42; // jump()

    if (onLadderAt(y)) {                  // guards, before the move
      if (v < -0.15) v = -0.15;
      if (sneaking(t) && v < 0) v = 0;
    }

    const prev = y;
    let next = y + v;
    if (v < 0) {                          // landing: clamp and cancel
      const land = surfaces
        .filter((s) => s <= y + EPS && next < s)
        .sort((a, b) => b - a)[0];
      if (land !== undefined) { next = land; v = 0; }
    }
    y = next;

    if (onLadderAt(y) && pressing(t)) v = 0.2; // climb, after the move
    v = (v - 0.08) * 0.98;                     // gravity + drag

    rows.push({ tick: t, y, dy: y - prev });
  }
  return rows;
}
```

Sanity checks:

- Flat jump (`ladder` out of reach, `surfaces: [0]`): apex 1.2492 on ticks 5–6,
  lands tick 12, the landing tick moves −0.1041.
- Reference ladder (`startY 0`, `ladder 1..1`, `surfaces [0, 2]`, jump, push
  every tick): lands tick 15. Push only on ticks 4–10: lands tick 13.

Note: the game's drag constant is the float `0.9800000190734863`; using `0.98`
does not change any result above at 4-decimal precision.

### Searching for the optimum

Branch each tick on push / no push, merge identical `(y, velocity)` states, and
prune any state that cannot reach the top by the best known landing tick even
climbing flat out. This is exact and fast (10 ladders in well under a second).
A search over a single contiguous press window is **not** enough — it misses the
mid-climb release.

---

## 7. Gotchas for implementers

- A scenario with no jump must start from velocity **−0.0784**, not 0, or
  every tick is off by one.
- A landing tick's ΔY is the truncated move, not the velocity. After landing,
  velocity is −0.0784, not 0.
- Compare positions with a small epsilon before flooring — accumulated
  `y += 0.1176` drifts in the last bits.
- Releasing the climb (stay in column) vs leaving the column (strafe off) are
  different: only the latter lifts the −0.15 fall cap.
- Vines behave identically for `isOnLadder()` but have no collision box, so
  what counts as `isCollidedHorizontally` depends on the block behind them.
