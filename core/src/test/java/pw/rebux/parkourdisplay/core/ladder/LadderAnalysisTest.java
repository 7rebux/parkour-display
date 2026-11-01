package pw.rebux.parkourdisplay.core.ladder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.function.IntPredicate;
import org.junit.jupiter.api.Test;
import pw.rebux.parkourdisplay.core.util.TestTickPositions;
import pw.rebux.parkourdisplay.core.util.TickPosition;

class LadderAnalysisTest {

  /// Jumping from inside a one block ladder: a jam takes 9 ticks, starting on tick 2 takes 6.
  @Test
  void jamFromInsideLadderSuggestsLaterStart() {
    var analysis = LadderAnalysis.of(climb(0, 0, 1, tick -> true));

    assertEquals(0, analysis.climb().enterTick());
    assertEquals(9, analysis.climb().airtime());
    assertFalse(analysis.climbStartDelayHelpful());
    assertEquals(2, analysis.betterClimbStartTick());
  }

  @Test
  void delayedStartFromInsideLadderIsHelpful() {
    var analysis = LadderAnalysis.of(climb(0, 0, 1, tick -> tick >= 2 && tick <= 3));

    assertEquals(6, analysis.climb().airtime());
    assertTrue(analysis.climbStartDelayHelpful());
    assertNull(analysis.betterClimbStartTick());
    assertEquals(6, analysis.optimalStrategies().getFirst().ticks());
  }

  @Test
  void fallingOnLadderBeforeClimbingMissesSneak() {
    var climb = climb(3, 0, 5, tick -> tick >= 8);

    assertTrue(climb.vy(climb.climbStartTick()) < 0);
    assertTrue(climb.isSneakMissed());
  }

  @Test
  void climbingWhileRisingDoesNotMissSneak() {
    assertFalse(climb(0, 0, 1, tick -> true).isSneakMissed());
  }

  /// The fall before the climb is too short for the higher start to save a tick.
  @Test
  void sneakNotSavingAirtimeIsNotMissed() {
    var climb = climb(1.5, 0, 3, tick -> tick >= 7);

    assertTrue(climb.vy(climb.climbStartTick()) < 0);
    assertFalse(climb.isSneakMissed());
  }

  /// Neither sneaking nor an early exit saves a tick on its own, but together they save two.
  @Test
  void combinedTricksSaveAirtime() {
    var climb = climb(0.25, 0, 2, tick -> tick >= 7);

    assertFalse(climb.isSneakMissed());
    assertFalse(climb.isEarlyExitMissed());

    var combination = climb.missedTrickCombination();
    assertNotNull(combination);
    assertEquals(EnumSet.of(MissedTrick.SNEAK, MissedTrick.EARLY_EXIT), combination.tricks());
    assertEquals(2, combination.savedTicks());
  }

  @Test
  void noCombinationWithoutMissedTricks() {
    assertNull(climb(0, 0, 1, tick -> tick >= 2 && tick <= 3).missedTrickCombination());
  }

  /// Simulates a jump from startY onto a ladder stack, climbing on the ticks press accepts, until
  /// it lands on the ladder top.
  private static LadderClimb climb(double startY, int ladderFrom, int topY, IntPredicate press) {
    var ticks = new ArrayList<TickPosition>();
    ticks.add(TestTickPositions.of(startY, 0, true, onLadder(startY, ladderFrom, topY)));

    var y = startY;
    var v = LadderClimb.JUMP_VY;

    for (int tick = 1; tick <= 100; tick++) {
      if (Math.abs(v) < 0.005) v = 0;
      if (onLadder(y, ladderFrom, topY) && v < -0.15) v = -0.15;

      if (v < 0 && y >= topY && y + v < topY) {
        ticks.add(TestTickPositions.of(topY, topY - y, true, false));
        return new LadderClimb(ticks, ladderFrom, topY);
      }

      y += v;
      var onLadder = onLadder(y, ladderFrom, topY);
      ticks.add(TestTickPositions.of(y, v, false, onLadder));
      v = ((onLadder && press.test(tick) ? 0.2 : v) - 0.08) * 0.9800000190734863;
    }

    throw new IllegalStateException("never landed on the ladder top");
  }

  private static boolean onLadder(double y, int ladderFrom, int topY) {
    var block = Math.floor(y + 1e-9);
    return block >= ladderFrom && block < topY;
  }
}
