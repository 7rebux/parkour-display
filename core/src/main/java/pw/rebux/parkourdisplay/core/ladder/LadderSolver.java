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
  /// @param fallsPastStart the start surface is not under the ladder column, so the jump can fall
  ///                       below startY instead of landing back on it.
  /// @return strategies sorted by airtime, then peak height, empty if the top cannot be reached
  ///         by jumping from startY.
  public static List<LadderStrategy> strategies(
      double startY,
      double ladderStartY,
      double ladderEndY,
      boolean fallsPastStart
  ) {
    var jump = Jump.of(startY, ladderStartY, ladderEndY, fallsPastStart);

    var plain = search(jump, tick -> true, MAX_TICKS);
    if (plain == null) return List.of();

    var paths = new ArrayList<Path>();
    paths.add(plain);
    paths.add(search(jump, null, plain.landingTick));

    for (var firstPress : firstPresses(jump)) {
      paths.addAll(climbs(jump, firstPress, MAX_TICKS));
    }

    return fastest(paths);
  }

  /// Like strategies, but every climb starts on firstPress.
  public static List<LadderStrategy> strategies(
      double startY,
      double ladderStartY,
      double ladderEndY,
      boolean fallsPastStart,
      int firstPress
  ) {
    var jump = Jump.of(startY, ladderStartY, ladderEndY, fallsPastStart);
    return fastest(climbs(jump, firstPress, MAX_TICKS));
  }

  private static List<LadderStrategy> fastest(List<@Nullable Path> paths) {
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
  public static List<Integer> climbStarts(
      double startY,
      double ladderStartY,
      double ladderEndY,
      boolean fallsPastStart
  ) {
    var jump = Jump.of(startY, ladderStartY, ladderEndY, fallsPastStart);
    var result = new ArrayList<Integer>();

    var y = startY;
    var v = JUMP;
    var belowTop = jump.belowTop(y);

    for (int tick = 1; result.size() < MAX_CLIMB_STARTS && tick <= MAX_TICKS; tick++) {
      if (Math.abs(v) < CUTOFF) v = 0;
      if (jump.onLadder(y) && v < FALL_CAP) v = FALL_CAP;
      if (belowTop && landsOn(y, v, jump.topY) || jump.ends(y, v)) break;

      y += v;
      v = drag(v);
      belowTop |= jump.belowTop(y);
      if (jump.onLadder(y)) result.add(tick);
    }

    return result;
  }

  /// @return the lowest airtime of a jump from startY that starts climbing on firstPress and plays
  ///         any combination of the allowed tricks afterwards, or null if it cannot land within
  ///         maxTicks.
  public static @Nullable Integer bestAirtime(
      double startY,
      double ladderStartY,
      double ladderEndY,
      boolean fallsPastStart,
      int firstPress,
      int maxTicks,
      boolean release,
      boolean earlyExit
  ) {
    var jump = Jump.of(startY, ladderStartY, ladderEndY, fallsPastStart);
    return climbs(jump, firstPress, maxTicks).stream()
        .filter(path -> {
          var tricks = LadderStrategy.of(path).tricks();
          return (release || !tricks.release()) && (earlyExit || !tricks.earlyExit());
        })
        .map(Path::landingTick)
        .min(Integer::compare)
        .orElse(null);
  }

  /// @return the climbs starting on firstPress: held to the end, with an early exit, and with a
  ///         one-tick release followed by an early exit or not.
  private static List<Path> climbs(Jump jump, int firstPress, int maxTicks) {
    // Unbounded, since the tricks are built from its presses even when it is too slow itself.
    var natural = search(jump, tick -> tick >= firstPress, MAX_TICKS);
    if (natural == null) return List.of();

    var paths = new ArrayList<@Nullable Path>();
    if (natural.landingTick() <= maxTicks) paths.add(natural);

    var presses = natural.inputs().stream()
        .filter(Input::pressed)
        .mapToInt(Input::tick)
        .toArray();
    if (presses.length == 0) return paths;
    var lastPress = presses[presses.length - 1];

    for (int exit = firstPress; exit < lastPress; exit++) {
      final var lastInput = exit;
      paths.add(search(jump, tick -> tick >= firstPress && tick <= lastInput, maxTicks));
    }

    // A release shifts the rest of the climb down by the same amount wherever it happens, so
    // the earliest one stands for all of them. The shift can push the natural end a tick later.
    final var released = presses[0] + 1;
    for (int exit = released + 1; released < lastPress && exit <= lastPress + 2; exit++) {
      final var lastInput = exit == lastPress + 2 ? Integer.MAX_VALUE : exit;
      paths.add(search(jump,
          tick -> tick >= firstPress && tick != released && tick <= lastInput, maxTicks));
    }

    return paths.stream().filter(Objects::nonNull).toList();
  }

  /// Jam on tick 1, plus every delayed start while the jump still rises faster than a climb would
  /// after touching the ladder.
  private static List<Integer> firstPresses(Jump jump) {
    var result = new ArrayList<Integer>();
    result.add(1);

    var y = jump.startY;
    var v = JUMP;
    var touched = false;

    for (int tick = 1; v > CLIMB_SPEED; tick++) {
      if (touched) result.add(tick);
      y += v;
      v = drag(v);
      touched |= jump.onLadder(y);
    }

    return result;
  }

  /// @param inputs whether to climb on a tick, or null to try both on every tick.
  private static @Nullable Path search(Jump jump, @Nullable IntPredicate inputs, int maxTicks) {
    return inputs == null
        ? search(jump, tick -> true, tick -> true, maxTicks)
        : search(jump, inputs, inputs.negate(), maxTicks);
  }

  /// @param canPress   whether climbing is tried on a tick.
  /// @param canRelease whether not climbing is tried on a tick.
  private static @Nullable Path search(
      Jump jump,
      IntPredicate canPress,
      IntPredicate canRelease,
      int maxTicks
  ) {
    var topY = jump.topY;
    var startBelowTop = jump.belowTop(jump.startY);
    var startPeak = startBelowTop ? jump.startY : Double.NEGATIVE_INFINITY;
    var states = List.of(
        new State(jump.startY, 0, startPeak, startBelowTop, true, 0, 0, null));

    for (int tick = 1; tick <= maxTicks && !states.isEmpty(); tick++) {
      var next = new HashMap<StateKey, State>();
      State landed = null;

      for (var state : states) {
        var y = state.y;
        var v = Math.abs(state.v) < CUTOFF ? 0 : state.v;

        if (tick == 1) v = JUMP;
        if (jump.onLadder(y) && v < FALL_CAP) v = FALL_CAP;

        var nextY = y + v;
        var belowTop = state.belowTop || jump.belowTop(nextY);

        if (state.belowTop && landsOn(y, v, topY)) {
          if (landed == null || state.compareTo(landed) < 0) landed = state;
          continue;
        }
        if (jump.ends(y, v)) continue;

        if (!jump.onLadder(nextY)) {
          merge(next, state.advance(nextY, drag(v), belowTop));
          continue;
        }

        if (canPress.test(tick)) {
          merge(next, state.press(tick, nextY, drag(CLIMB), belowTop, true));
        }
        if (canRelease.test(tick)) {
          merge(next, state.press(tick, nextY, drag(v), belowTop, false));
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

  /// @param ladderFrom lowest ladder block.
  /// @param ladderTo   highest ladder block.
  private record Jump(
      double startY,
      int ladderFrom,
      int ladderTo,
      double topY,
      boolean fallsPastStart
  ) {

    static Jump of(double startY, double ladderStartY, double ladderEndY, boolean fallsPastStart) {
      return new Jump(
          startY,
          (int) Math.floor(ladderStartY + EPSILON),
          (int) Math.ceil(ladderEndY - EPSILON) - 1,
          ladderEndY,
          fallsPastStart);
    }

    boolean onLadder(double y) {
      var block = Math.floor(y + EPSILON);
      return block >= this.ladderFrom && block <= this.ladderTo;
    }

    /// Landing on the top only counts after being below it: a jump from above the ladder falls
    /// past the top in the ladder column first.
    boolean belowTop(double y) {
      return y + EPSILON < this.topY;
    }

    /// @return whether this move lands back on the start surface or falls below the ladder,
    ///         from where the top cannot be reached anymore.
    boolean ends(double y, double v) {
      return !this.fallsPastStart && landsOn(y, v, this.startY)
          || v < 0 && Math.floor(y + v + EPSILON) < this.ladderFrom;
    }
  }

  private record StateKey(long y, long v, boolean belowTop, boolean lastPressed) {
  }

  /// @param history effective inputs, newest first: only ticks ending on the ladder are recorded,
  ///                since pressing anywhere else has no effect.
  private record State(
      double y,
      double v,
      double peakY,
      boolean belowTop,
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

    State advance(double y, double v, boolean belowTop) {
      return new State(
          y, v, this.peak(y, belowTop), belowTop, lastPressed, transitions, released, history);
    }

    State press(int tick, double y, double v, boolean belowTop, boolean pressed) {
      return new State(
          y,
          v,
          this.peak(y, belowTop),
          belowTop,
          pressed,
          transitions + (pressed != lastPressed ? 1 : 0),
          released + (pressed ? 0 : 1),
          new Input(tick, pressed, history));
    }

    /// Only the overshoot over the top counts, not the start of a jump from above the ladder.
    private double peak(double y, boolean belowTop) {
      return belowTop ? Math.max(peakY, y) : peakY;
    }

    StateKey key() {
      return new StateKey(
          Math.round(y / EPSILON), Math.round(v / EPSILON), belowTop, lastPressed);
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
