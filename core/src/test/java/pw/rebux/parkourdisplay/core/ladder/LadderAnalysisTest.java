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
