package pw.rebux.parkourdisplay.core.ladder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/// Expected airtimes come from docs/ladder-mechanics.md, section 5, or were confirmed by an
/// exhaustive search over every input sequence.
class LadderSolverTest {

  @ParameterizedTest
  @CsvSource({
      "1, 15, 13",
      "2, 22, 22",
      "3, 32, 30",
      "4, 41, 39",
      "5, 49, 47",
      "6, 58, 56",
      "7, 66, 64",
      "8, 75, 73",
      "9, 83, 81",
      "10, 92, 90",
  })
  void stackOneBlockUp(int ladders, int plain, int best) {
    var strategies = LadderSolver.strategies(0, 1, 1 + ladders);

    assertEquals(best, strategies.getFirst().ticks());
    assertEquals(plain, plainHold(strategies).ticks());
  }

  @Test
  void singleLadderNeedsPessiAndEarlyExit() {
    var best = LadderSolver.strategies(0, 1, 2).getFirst();

    assertEquals(3, best.pessi());
    assertTrue(best.releaseTicks().isEmpty());
    assertEquals(11, best.earlyExitTick());
  }

  @Test
  void twoLaddersOnlyNeedAPlainHold() {
    var strategies = LadderSolver.strategies(0, 1, 3);

    assertTrue(strategies.getFirst().isPlainHold());
    assertEquals(1, strategies.size());
  }

  @Test
  void fourLaddersNeedOnlyAnEarlyExit() {
    var best = LadderSolver.strategies(0, 1, 5).getFirst();

    assertNull(best.pessi());
    assertTrue(best.releaseTicks().isEmpty());
    assertEquals(37, best.earlyExitTick());
  }

  @Test
  void slabNeedsAllThreeTricks() {
    var best = LadderSolver.strategies(0.5, 1, 3).getFirst();

    assertEquals(18, best.ticks());
    assertEquals(3, best.pessi());
    assertEquals(1, best.releaseTicks().size());
    assertEquals(16, best.earlyExitTick());
  }

  @ParameterizedTest
  @CsvSource({"1, 6", "2, 13", "3, 22", "4, 30", "5, 39"})
  void stackAtFeet(int ladders, int best) {
    assertEquals(best, LadderSolver.strategies(0, 0, ladders).getFirst().ticks());
  }

  @Test
  void unreachableTopHasNoStrategies() {
    assertTrue(LadderSolver.strategies(2, 0, 2).isEmpty());
  }

  @Test
  void climbStartsBeginOnContact() {
    assertEquals(List.of(3, 4, 5, 6, 7, 8), LadderSolver.climbStarts(0, 1, 2));
  }

  @Test
  void bestAirtimeFromEachStart() {
    assertEquals(14, LadderSolver.bestAirtime(0, 1, 2, 3, Integer.MAX_VALUE));
    assertEquals(13, LadderSolver.bestAirtime(0, 1, 2, 4, Integer.MAX_VALUE));
  }

  /// The plain hold from this start takes longer than the limit, but its tricks don't.
  @Test
  void bestAirtimeFindsTricksBeyondASlowPlainHold() {
    assertEquals(16, LadderSolver.bestAirtime(0, 0, 2, 7, 17));
    assertNull(LadderSolver.bestAirtime(0, 0, 2, 7, 15));
  }

  /// Raised starts and stacks at the feet, beyond the doc's examples.
  @ParameterizedTest
  @CsvSource({
      // startY, ladderFrom, ladders, ticks, climb, pessi, release, earlyExit
      "0.75,    0, 1, 11, false,  , false, false",
      "0.9375,  0, 1, 12, false,  , false, false",
      "0.5,     0, 1,  5, true,   , false, false",
      "0.3125,  0, 1,  5, true,   , false, true",
      "0.9375,  0, 2,  7, true,  2, false, false",
      "0.5,     0, 2,  9, true,  4, false, true",
      "0.3125,  0, 3, 19, true,  4, false, true",
      "0.125,   0, 3, 21, true,  3, true,  true",
      "0.3125,  1, 1, 11, true,  3, true,  true",
      "0.5,     1, 2, 18, true,  3, true,  true",
      "0.125,   1, 4, 38, true,  3, true,  true",
      "0.75,    1, 4, 32, true,  3, false, true",
  })
  void bestStrategy(
      double startY,
      int ladderFrom,
      int ladders,
      int ticks,
      boolean climb,
      @Nullable Integer pessi,
      boolean release,
      boolean earlyExit
  ) {
    var best = LadderSolver.strategies(startY, ladderFrom, ladderFrom + ladders).getFirst();

    assertEquals(ticks, best.ticks());
    assertEquals(climb, best.climbNeeded());
    assertEquals(pessi, best.pessi());
    assertEquals(release, !best.releaseTicks().isEmpty());
    assertEquals(earlyExit, best.earlyExitTick() != null);
  }

  /// Each entry is a climb start and the best airtime when climbing from it.
  @ParameterizedTest
  @CsvSource({
      "0,       1, 1, '3:14 4:13 5:14 6:15 7:16 8:19'",
      "0,       1, 4, '3:39 4:39 5:40 6:41 7:42 8:45'",
      "0,       0, 1, '1:9 2:6'",
      "0.3125,  1, 1, '2:13 3:12 4:11 5:12 6:13 7:14 8:17 9:19 10:20'",
      "0.5,     0, 2, '1:12 2:11 3:9 4:10 5:9 6:10 7:13 8:14 9:17 10:19'",
      "0.9375,  0, 2, '1:9 2:8 3:7'",
  })
  void bestAirtimePerClimbStart(double startY, int ladderFrom, int ladders, String expected) {
    var topY = ladderFrom + ladders;
    var starts = new ArrayList<Integer>();
    var airtimes = new ArrayList<@Nullable Integer>();
    var expectedAirtimes = new ArrayList<Integer>();

    for (var entry : expected.split(" ")) {
      var parts = entry.split(":");
      starts.add(Integer.parseInt(parts[0]));
      expectedAirtimes.add(Integer.parseInt(parts[1]));
    }
    for (var start : starts) {
      airtimes.add(LadderSolver.bestAirtime(startY, ladderFrom, topY, start, Integer.MAX_VALUE));
    }

    assertEquals(starts, LadderSolver.climbStarts(startY, ladderFrom, topY));
    assertEquals(expectedAirtimes, airtimes);
  }

  private static LadderStrategy plainHold(List<LadderStrategy> strategies) {
    return strategies.stream().filter(LadderStrategy::isPlainHold).findFirst().orElseThrow();
  }
}
