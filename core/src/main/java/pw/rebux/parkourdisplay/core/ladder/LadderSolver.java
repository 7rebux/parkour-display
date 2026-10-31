package pw.rebux.parkourdisplay.core.ladder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/// Finds input sequences with the lowest airtime for jumping onto and climbing a ladder stack,
/// following the 1.8.9 tick simulation described in docs/ladder-mechanics.md.
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LadderSolver {

  private static final double EPSILON = 1.0E-9;
  private static final double GRAVITY = 0.08;
  private static final double DRAG = 0.9800000190734863;
  private static final double JUMP = 0.42;
  private static final double CLIMB = 0.2;
  private static final double FALL_CAP = -0.15;
  private static final double CUTOFF = 0.005;
  private static final double CLIMB_SPEED = (CLIMB - GRAVITY) * DRAG;
  private static final int MAX_TICKS = 2000;
  private static final int MAX_CLIMB_STARTS = 10;

  /// Lowest airtime first, then the biggest overshoot, which leaves the most room for error.
  private static final Comparator<LadderStrategy> BEST = Comparator
      .comparingInt(LadderStrategy::ticks)
      .thenComparing(Comparator.comparingDouble(LadderStrategy::peakHeight).reversed());

  /// Combines a delayed start, a one-tick release and an early exit in every way, keeping the
  /// fastest variant of each combination that beats all of its simpler combinations.
  ///
  /// @return strategies sorted by airtime, then peak height, empty if the top cannot be reached
  ///         by jumping from startY.
  public static List<LadderStrategy> strategies(
      double startY,
      double ladderStartY,
      double ladderEndY
  ) {
    var ladderFrom = (int) Math.floor(ladderStartY + EPSILON);
    var ladderTo = (int) Math.ceil(ladderEndY - EPSILON) - 1;
    var topY = ladderEndY;

    if (topY <= startY + EPSILON) return List.of();

    var plain = search(startY, ladderFrom, ladderTo, topY, tick -> true, MAX_TICKS);
    if (plain == null) return List.of();

    var paths = new ArrayList<Path>();
    paths.add(plain);
    paths.add(search(startY, ladderFrom, ladderTo, topY, null, plain.landingTick));

    for (var firstPress : firstPresses(startY, ladderFrom, ladderTo)) {
      var natural = search(startY, ladderFrom, ladderTo, topY,
          tick -> tick >= firstPress, MAX_TICKS);
      if (natural == null) continue;
      paths.add(natural);

      var lastPress = natural.inputs().stream()
          .filter(Input::pressed)
          .mapToInt(Input::tick)
          .max()
          .orElse(firstPress);

      for (int exit = firstPress; exit < lastPress; exit++) {
        final var lastInput = exit;
        paths.add(search(startY, ladderFrom, ladderTo, topY,
            tick -> tick >= firstPress && tick <= lastInput, MAX_TICKS));
      }

      // A release lowers the rest of the climb, which can push the natural end a tick later.
      for (int release = firstPress + 1; release < lastPress; release++) {
        final var released = release;
        for (int exit = release + 1; exit <= lastPress + 2; exit++) {
          final var lastInput = exit == lastPress + 2 ? Integer.MAX_VALUE : exit;
          paths.add(search(startY, ladderFrom, ladderTo, topY,
              tick -> tick >= firstPress && tick != released && tick <= lastInput, MAX_TICKS));
        }
      }
    }

    var fastest = new LinkedHashMap<LadderStrategy.Tricks, LadderStrategy>();
    paths.stream()
        .filter(Objects::nonNull)
        .map(LadderStrategy::of)
        .forEach(strategy -> fastest.merge(strategy.tricks(), strategy,
            (a, b) -> BEST.compare(b, a) < 0 ? b : a));

    return fastest.values().stream()
        .filter(strategy -> fastest.values().stream().noneMatch(simpler ->
            simpler != strategy
                && BEST.compare(simpler, strategy) <= 0
                && simpler.tricks().isSubsetOf(strategy.tricks())))
        .sorted(BEST.thenComparingInt(strategy -> strategy.tricks().count()))
        .toList();
  }

  /// @return ticks of a jump from startY that end on the ladder without climbing, so the climb can
  ///         start on them. The first one is the contact tick.
  public static List<Integer> climbStarts(double startY, double ladderStartY, double ladderEndY) {
    var ladderFrom = (int) Math.floor(ladderStartY + EPSILON);
    var ladderTo = (int) Math.ceil(ladderEndY - EPSILON) - 1;
    var result = new ArrayList<Integer>();

    var y = startY;
    var v = JUMP;

    for (int tick = 1; result.size() < MAX_CLIMB_STARTS && tick <= MAX_TICKS; tick++) {
      if (Math.abs(v) < CUTOFF) v = 0;
      if (onLadder(y, ladderFrom, ladderTo) && v < FALL_CAP) v = FALL_CAP;
      if (landsOn(y, v, ladderEndY) || landsOn(y, v, startY)) break;

      y += v;
      v = drag(v);
      if (onLadder(y, ladderFrom, ladderTo)) result.add(tick);
    }

    return result;
  }

  /// @return the lowest airtime of a jump from startY that starts climbing on firstPress and plays
  ///         any input afterwards, or null if it cannot land within maxTicks.
  public static @Nullable Integer bestAirtime(
      double startY,
      double ladderStartY,
      double ladderEndY,
      int firstPress,
      int maxTicks
  ) {
    var ladderFrom = (int) Math.floor(ladderStartY + EPSILON);
    var ladderTo = (int) Math.ceil(ladderEndY - EPSILON) - 1;
    var path = search(startY, ladderFrom, ladderTo, ladderEndY,
        tick -> tick >= firstPress, tick -> tick != firstPress, maxTicks);
    return path == null ? null : path.landingTick();
  }

  /// Jam on tick 1, plus every delayed start while the jump still rises faster than a climb would
  /// after touching the ladder.
  private static List<Integer> firstPresses(double startY, int ladderFrom, int ladderTo) {
    var result = new ArrayList<Integer>();
    result.add(1);

    var y = startY;
    var v = JUMP;
    var touched = false;

    for (int tick = 1; v > CLIMB_SPEED; tick++) {
      if (touched) result.add(tick);
      y += v;
      v = drag(v);
      touched |= onLadder(y, ladderFrom, ladderTo);
    }

    return result;
  }

  /// @param inputs whether to climb on a tick, or null to try both on every tick.
  private static @Nullable Path search(
      double startY,
      int ladderFrom,
      int ladderTo,
      double topY,
      @Nullable IntPredicate inputs,
      int maxTicks
  ) {
    return inputs == null
        ? search(startY, ladderFrom, ladderTo, topY, tick -> true, tick -> true, maxTicks)
        : search(startY, ladderFrom, ladderTo, topY, inputs, inputs.negate(), maxTicks);
  }

  /// @param canPress   whether climbing is tried on a tick.
  /// @param canRelease whether not climbing is tried on a tick.
  private static @Nullable Path search(
      double startY,
      int ladderFrom,
      int ladderTo,
      double topY,
      IntPredicate canPress,
      IntPredicate canRelease,
      int maxTicks
  ) {
    var states = List.of(new State(startY, 0, startY, true, 0, 0, null));

    for (int tick = 1; tick <= maxTicks && !states.isEmpty(); tick++) {
      var next = new HashMap<StateKey, State>();
      State landed = null;

      for (var state : states) {
        var y = state.y;
        var v = Math.abs(state.v) < CUTOFF ? 0 : state.v;

        if (tick == 1) v = JUMP;
        if (onLadder(y, ladderFrom, ladderTo) && v < FALL_CAP) v = FALL_CAP;

        var nextY = y + v;

        if (v < 0) {
          if (y + EPSILON >= topY && nextY < topY) {
            if (landed == null || state.compareTo(landed) < 0) landed = state;
            continue;
          }
          if (y + EPSILON >= startY && nextY < startY) continue;
        }

        if (!onLadder(nextY, ladderFrom, ladderTo)) {
          merge(next, state.advance(nextY, drag(v)));
          continue;
        }

        if (canPress.test(tick)) {
          merge(next, state.press(tick, nextY, drag(CLIMB), true));
        }
        if (canRelease.test(tick)) {
          merge(next, state.press(tick, nextY, drag(v), false));
        }
      }

      if (landed != null) return new Path(tick, landed.peakY - topY, landed.inputs());

      final var completed = tick;
      states = next.values().stream()
          .filter(state -> lowerBound(completed, state, topY) <= maxTicks)
          .toList();
    }

    return null;
  }

  private static boolean landsOn(double y, double v, double surfaceY) {
    return v < 0 && y + EPSILON >= surfaceY && y + v < surfaceY;
  }

  private static boolean onLadder(double y, int ladderFrom, int ladderTo) {
    var block = Math.floor(y + EPSILON);
    return block >= ladderFrom && block <= ladderTo;
  }

  private static double drag(double v) {
    return (v - GRAVITY) * DRAG;
  }

  /// No future tick can move up faster than max(v, CLIMB_SPEED), and landing takes one more tick.
  private static int lowerBound(int tick, State state, double topY) {
    var remaining = topY - state.y;
    if (remaining <= EPSILON) return tick + 1;
    var speed = Math.max(state.v, CLIMB_SPEED);
    return tick + (int) Math.ceil(remaining / speed - EPSILON) + 1;
  }

  private static void merge(HashMap<StateKey, State> states, State state) {
    states.merge(state.key(), state, (a, b) -> a.compareTo(b) <= 0 ? a : b);
  }

  private record StateKey(long y, long v, boolean lastPressed) {
  }

  /// @param history effective inputs, newest first: only ticks ending on the ladder are recorded,
  ///                since pressing anywhere else has no effect.
  private record State(
      double y,
      double v,
      double peakY,
      boolean lastPressed,
      int transitions,
      int released,
      @Nullable Input history
  ) implements Comparable<State> {

    private static final Comparator<State> PREFERRED = Comparator
        .comparingDouble(State::peakY)
        .reversed()
        .thenComparingInt(State::transitions)
        .thenComparingInt(State::released);

    State advance(double y, double v) {
      return new State(y, v, Math.max(peakY, y), lastPressed, transitions, released, history);
    }

    State press(int tick, double y, double v, boolean pressed) {
      return new State(
          y,
          v,
          Math.max(peakY, y),
          pressed,
          transitions + (pressed != lastPressed ? 1 : 0),
          released + (pressed ? 0 : 1),
          new Input(tick, pressed, history));
    }

    StateKey key() {
      return new StateKey(Math.round(y / EPSILON), Math.round(v / EPSILON), lastPressed);
    }

    List<Input> inputs() {
      var inputs = new ArrayList<Input>();
      for (var input = history; input != null; input = input.previous) inputs.add(0, input);
      return inputs;
    }

    @Override
    public int compareTo(State other) {
      return PREFERRED.compare(this, other);
    }
  }

  record Input(int tick, boolean pressed, @Nullable Input previous) {
  }

  /// @param peakHeight highest feet Y reached, relative to the top of the ladder.
  record Path(int landingTick, double peakHeight, List<Input> inputs) {
  }
}
