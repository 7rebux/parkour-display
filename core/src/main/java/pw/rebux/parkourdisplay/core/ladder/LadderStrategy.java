package pw.rebux.parkourdisplay.core.ladder;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.core.ladder.LadderSolver.Input;
import pw.rebux.parkourdisplay.core.ladder.LadderSolver.Path;

/// @param peakHeight    highest feet Y reached, relative to the top of the ladder.
/// @param pessi         ticks between the jump and the first climb input, or null for a jam.
/// @param releaseTicks  ticks on which the climb is released for one tick mid-climb.
/// @param earlyExitTick tick on which the climb is released for good before reaching the top.
public record LadderStrategy(
    int ticks,
    boolean climbNeeded,
    double peakHeight,
    @Nullable Integer pessi,
    List<Integer> releaseTicks,
    @Nullable Integer earlyExitTick
) {

  static LadderStrategy of(Path path) {
    var inputs = path.inputs();
    var presses = inputs.stream().filter(Input::pressed).map(Input::tick).toList();

    if (presses.isEmpty()) {
      return new LadderStrategy(
          path.landingTick(),
          false,
          path.peakHeight(),
          null,
          List.of(),
          null);
    }

    var firstPress = presses.getFirst();
    var lastPress = presses.getLast();
    var contactTick = inputs.getFirst().tick();

    var releaseTicks = inputs.stream()
        .filter(input -> !input.pressed())
        .map(Input::tick)
        .filter(tick -> tick > firstPress && tick < lastPress)
        .toList();
    var earlyExitTick = inputs.stream()
        .filter(input -> !input.pressed() && input.tick() > lastPress)
        .map(Input::tick)
        .findFirst()
        .orElse(null);

    return new LadderStrategy(
        path.landingTick(),
        true,
        path.peakHeight(),
        firstPress > contactTick ? firstPress - 1 : null,
        releaseTicks,
        earlyExitTick);
  }

  public boolean isPlainHold() {
    return climbNeeded && pessi == null && releaseTicks.isEmpty() && earlyExitTick == null;
  }

  Tricks tricks() {
    return new Tricks(climbNeeded, pessi, !releaseTicks.isEmpty(), earlyExitTick != null);
  }

  record Tricks(
      boolean climb,
      @Nullable Integer pessi,
      boolean release,
      boolean earlyExit
  ) {

    /// Not climbing at all is simpler than any climb, a jam simpler than any pessi.
    boolean isSubsetOf(Tricks other) {
      if (!climb) return true;
      return other.climb
          && (pessi == null || Objects.equals(pessi, other.pessi))
          && (!release || other.release)
          && (!earlyExit || other.earlyExit);
    }

    int count() {
      return (climb ? 1 : 0) + (pessi != null ? 1 : 0) + (release ? 1 : 0) + (earlyExit ? 1 : 0);
    }
  }
}
