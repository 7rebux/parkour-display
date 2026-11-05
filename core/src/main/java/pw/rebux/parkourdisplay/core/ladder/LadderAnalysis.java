package pw.rebux.parkourdisplay.core.ladder;

import java.util.List;
import org.jspecify.annotations.Nullable;

/// The checks of a LadderClimb that need the LadderSolver. Building one runs several exhaustive
/// searches, so keep it off the game thread.
///
/// @param climbStartDelayHelpful the climb started later than possible, and every earlier start
///                               would have been slower.
/// @param betterClimbStartTick   airtime of the climb start that allows the lowest airtime with
///                               the tricks that were played, if it beats the actual one.
/// @param optimalStrategies      the strategies with the lowest possible airtime for this jump.
public record LadderAnalysis(
    LadderClimb climb,
    boolean climbStartDelayHelpful,
    @Nullable Integer betterClimbStartTick,
    List<LadderStrategy> optimalStrategies
) {

  private static final double Y_EPSILON = 1e-6;

  /// The solver only models jumping straight into the ladder column, so a climb that touched the
  /// ladder on a different tick than simulated (walked in, jumped from afar) is not analyzed.
  ///
  /// @param keepClimbStart only consider optimal strategies that start climbing on the same tick.
  public static LadderAnalysis of(LadderClimb climb, boolean keepClimbStart) {
    var unanalyzed = new LadderAnalysis(climb, false, null, List.of());
    if (climb.ticks().size() < 2 || !climb.moved(1, LadderClimb.JUMP_VY)) return unanalyzed;

    var startY = climb.y(0);
    var starts = LadderSolver.climbStarts(startY, climb.ladderBottomY(), climb.ladderTopY());
    if (starts.isEmpty() || starts.getFirst() != climb.firstClimbStartTick()) return unanalyzed;

    var optimal = optimalStrategies(climb, keepClimbStart);
    var start = climb.climbStartTick();
    if (start == -1) return new LadderAnalysis(climb, false, null, optimal);

    // Compares the starts with only the tricks that were played after them, so neither a missed
    // trick nor one that a different start merely enables counts as a better start.
    var airtime = bestAirtime(climb, start, Integer.MAX_VALUE);
    if (airtime == null) return new LadderAnalysis(climb, false, null, optimal);

    var delayHelpful = start > starts.getFirst();
    Integer better = null;
    var betterAirtime = airtime;

    for (var other : starts) {
      if (other == start) continue;

      var otherAirtime = bestAirtime(climb, other, airtime);
      if (otherAirtime == null) continue;

      if (other < start) delayHelpful = false;
      if (otherAirtime < betterAirtime || otherAirtime == betterAirtime && better != null
          && Math.abs(other - start) < Math.abs(better - start)) {
        better = other;
        betterAirtime = otherAirtime;
      }
    }

    return new LadderAnalysis(climb, delayHelpful, better, optimal);
  }

  private static List<LadderStrategy> optimalStrategies(LadderClimb climb, boolean keepClimbStart) {
    if (Math.abs(climb.y(climb.airtime()) - climb.ladderTopY()) > Y_EPSILON) return List.of();

    var start = climb.climbStartTick();
    var strategies = keepClimbStart && start != -1
        ? LadderSolver.strategies(climb.y(0), climb.ladderBottomY(), climb.ladderTopY(), start)
        : LadderSolver.strategies(climb.y(0), climb.ladderBottomY(), climb.ladderTopY());
    if (strategies.isEmpty()) return List.of();

    var fastest = strategies.getFirst().ticks();
    return strategies.stream().filter(strategy -> strategy.ticks() == fastest).toList();
  }

  private static @Nullable Integer bestAirtime(LadderClimb climb, int firstPress, int maxTicks) {
    return LadderSolver.bestAirtime(
        climb.y(0), climb.ladderBottomY(), climb.ladderTopY(), firstPress, maxTicks,
        !climb.releaseTicks().isEmpty(), climb.isEarlyExit());
  }
}
