package pw.rebux.parkourdisplay.core.ladder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.function.IntPredicate;
import org.junit.jupiter.api.Test;
import pw.rebux.parkourdisplay.core.util.TestTickPositions;
import pw.rebux.parkourdisplay.core.util.TickPosition;

class LadderAnalysisTest {

  /// Jumping from inside a one block ladder: a jam takes 9 ticks, starting on tick 2 takes 6.
  @Test
  void jamFromInsideLadderSuggestsLaterStart() {
    var analysis = LadderAnalysis.of(climb(0, 0, 1, tick -> true), false);

    assertEquals(0, analysis.climb().enterTick());
    assertEquals(9, analysis.climb().airtime());
    assertFalse(analysis.climbStartDelayHelpful());
    assertEquals(2, analysis.betterClimbStartTick());
  }

  @Test
  void delayedStartFromInsideLadderIsHelpful() {
    var analysis = LadderAnalysis.of(climb(0, 0, 1, tick -> tick >= 2 && tick <= 3), false);

    assertEquals(6, analysis.climb().airtime());
    assertTrue(analysis.climbStartDelayHelpful());
    assertNull(analysis.betterClimbStartTick());
    assertEquals(6, analysis.optimalStrategies().getFirst().ticks());
  }

  /// A pessi 3t only saves airtime by enabling an early exit, which the jam did not play.
  @Test
  void startOnlyBetterWithUnplayedTrickIsNotMissed() {
    var analysis = LadderAnalysis.of(climb(0, 1, 2, tick -> true), false);

    assertEquals(15, analysis.climb().airtime());
    assertNull(analysis.betterClimbStartTick());
  }

  @Test
  void optimalKeepingClimbStartOnlyChangesLaterTricks() {
    var jam = climb(0, 1, 2, tick -> true);

    assertEquals(13, LadderAnalysis.of(jam, false).optimalStrategies().getFirst().ticks());

    var kept = LadderAnalysis.of(jam, true).optimalStrategies().getFirst();
    assertEquals(14, kept.ticks());
    assertNull(kept.pessi());
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

  @Test
  void jumpFromAboveLadderIsAnalyzed() {
    var analysis = LadderAnalysis.of(climb(5, 0, 3, tick -> true), false);

    assertTrue(analysis.climb().isClimb());
    assertTrue(analysis.climb().isFallPastStart());
    assertEquals(19, analysis.climb().airtime());
    assertEquals(19, analysis.optimalStrategies().getFirst().ticks());
  }

  @Test
  void delayedClimbAfterFallingIntoLadderIsAnalyzed() {
    var climb = climb(4.5, 2, 4, tick -> tick >= 13);
    var analysis = LadderAnalysis.of(climb, false);

    assertTrue(climb.isFallPastStart());
    assertEquals(13, climb.enterTick());
    assertEquals(18, analysis.optimalStrategies().getFirst().ticks());
  }

  /// Simulates a jump from startY onto a ladder stack, climbing on the ticks press accepts, until
  /// it lands on the ladder top.
  private static LadderClimb climb(double startY, int ladderFrom, int topY, IntPredicate press) {
    var ticks = new ArrayList<TickPosition>();
    ticks.add(TestTickPositions.of(startY, 0, true, onLadder(startY, ladderFrom, topY)));

    var y = startY;
    var v = LadderClimb.JUMP_VY;
    var belowTop = startY < topY;

    for (int tick = 1; tick <= 100; tick++) {
      if (Math.abs(v) < 0.005) v = 0;
      if (onLadder(y, ladderFrom, topY) && v < -0.15) v = -0.15;

      if (belowTop && v < 0 && y >= topY && y + v < topY) {
        ticks.add(TestTickPositions.of(topY, topY - y, true, false));
        return new LadderClimb(ticks, ladderFrom, topY);
      }

      y += v;
      belowTop |= y < topY;
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
