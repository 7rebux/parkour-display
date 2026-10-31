package pw.rebux.parkourdisplay.core.ladder;

import java.util.List;
import java.util.stream.IntStream;
import pw.rebux.parkourdisplay.core.util.TickPosition;

/// One airborne period that touched a ladder, from the last tick on the ground to the landing
/// tick. Indices into ticks equal the airtime of that tick.
///
/// @param ladderBottomY bottom of the ladder stack that was climbed.
/// @param ladderTopY    top of the ladder stack that was climbed.
public record LadderClimb(List<TickPosition> ticks, double ladderBottomY, double ladderTopY) {

  public static final double JUMP_VY = 0.42;
  public static final double CLIMB_VY = 0.1176;
  /// Velocity left after releasing a climb tick: (0.1176 - 0.08) * 0.98
  public static final double RELEASE_VY = 0.0368;
  /// Height lost by releasing for one tick mid-climb.
  public static final double RELEASE_SHIFT = CLIMB_VY - RELEASE_VY;

  private static final double VY_EPSILON = 1e-3;
  private static final double VELOCITY_CUTOFF = 0.005;
  private static final int MAX_LANDING_TICKS = 20;

  public int airtime() {
    return this.ticks.size() - 1;
  }

  /// @return airtime of the first tick on the ladder.
  public int enterTick() {
    return IntStream.range(0, this.ticks.size())
        .filter(tick -> this.ticks.get(tick).onClimbable())
        .findFirst()
        .orElseThrow();
  }

  /// @return airtime of the first tick off the ladder after the climb.
  public int exitTick() {
    return IntStream.range(0, this.ticks.size())
        .filter(tick -> this.ticks.get(tick).onClimbable())
        .max()
        .orElseThrow() + 1;
  }

  /// @return airtime of the tick that moved the feet above the ladder top, or -1 if the top was
  ///         never reached.
  public int crossingTick() {
    return IntStream.range(this.enterTick() + 1, this.ticks.size())
        .filter(tick -> this.y(tick - 1) < this.ladderTopY && this.y(tick) >= this.ladderTopY)
        .findFirst()
        .orElse(-1);
  }

  /// @return airtime of the first tick whose climb input moved the player, or -1 without climbing.
  public int climbStartTick() {
    return IntStream.range(0, this.ticks.size() - 1)
        .filter(tick -> this.ticks.get(tick).onClimbable() && this.moved(tick + 1, CLIMB_VY))
        .findFirst()
        .orElse(-1);
  }

  public double y(int tick) {
    return this.ticks.get(tick).y();
  }

  public double vy(int tick) {
    return this.ticks.get(tick).vy();
  }

  public boolean isEarlyExit() {
    var crossing = this.crossingTick();
    return crossing != -1 && this.moved(crossing, RELEASE_VY);
  }

  /// Crossed the top with a climb tick, but releasing one tick earlier would have turned it into
  /// a 0.0368 tick that still clears the top.
  public boolean isEarlyExitMissed() {
    var crossing = this.crossingTick();
    return crossing != -1
        && this.moved(crossing, CLIMB_VY)
        && this.y(crossing - 1) + RELEASE_VY >= this.ladderTopY;
  }

  /// @return airtimes of the 0.0368 ticks that sit between two climb ticks while on the ladder.
  public List<Integer> releaseTicks() {
    var crossing = this.crossingTick();
    var end = crossing == -1 ? this.ticks.size() - 1 : crossing;
    return IntStream.range(this.enterTick() + 1, end)
        .filter(tick -> this.ticks.get(tick).onClimbable()
            && this.moved(tick - 1, CLIMB_VY)
            && this.moved(tick, RELEASE_VY)
            && this.moved(tick + 1, CLIMB_VY))
        .boxed()
        .toList();
  }

  /// A release only pays together with an early exit. Without the releases the same inputs would
  /// have left the climb higher, so compare the landings from both heights.
  public boolean isReleaseHelpful() {
    var releases = this.releaseTicks().size();
    if (releases == 0 || !this.isEarlyExit()) return false;

    var fromY = this.y(this.crossingTick() - 1);
    return this.landingTicks(fromY + releases * RELEASE_SHIFT, RELEASE_VY)
        > this.landingTicks(fromY, RELEASE_VY);
  }

  /// Releasing on the second to last climb tick needs a climb tick before and after it, and the
  /// lowered position must still be on the ladder to resume climbing. From there the last climb
  /// tick is followed by an early exit instead of another climb tick.
  public boolean isReleaseMissed() {
    var crossing = this.crossingTick();
    if (crossing < 3 || !this.releaseTicks().isEmpty() || this.isEarlyExit()
        || this.isEarlyExitMissed()) {
      return false;
    }

    var fromY = this.y(crossing - 1);
    return this.moved(crossing, CLIMB_VY)
        && this.moved(crossing - 1, CLIMB_VY)
        && this.moved(crossing - 2, CLIMB_VY)
        && fromY - RELEASE_SHIFT >= this.ladderBottomY
        && this.landingTicks(fromY - RELEASE_SHIFT, CLIMB_VY)
        < this.landingTicks(fromY, CLIMB_VY);
  }

  boolean moved(int tick, double vy) {
    return Math.abs(this.vy(tick) - vy) < VY_EPSILON;
  }

  /// Ticks until landing on the ladder top without climbing, or MAX_LANDING_TICKS + 1 if the top
  /// is never cleared.
  private int landingTicks(double y, double vy) {
    for (int ticks = 1; ticks <= MAX_LANDING_TICKS; ticks++) {
      if (Math.abs(vy) < VELOCITY_CUTOFF) vy = 0;
      var nextY = y + vy;
      if (vy < 0 && y >= this.ladderTopY && nextY < this.ladderTopY) return ticks;
      y = nextY;
      vy = (vy - 0.08) * 0.98;
    }
    return MAX_LANDING_TICKS + 1;
  }
}
