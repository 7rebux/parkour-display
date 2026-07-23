package pw.rebux.parkourdisplay.common.run;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.common.geom.Vec3;
import pw.rebux.parkourdisplay.common.platform.Message;
import pw.rebux.parkourdisplay.common.platform.ParkourContext;
import pw.rebux.parkourdisplay.common.platform.TextColorType;
import pw.rebux.parkourdisplay.common.state.PlayerState;
import pw.rebux.parkourdisplay.common.util.BoundingBoxUtils;
import pw.rebux.parkourdisplay.common.util.TickFormatter;

@Data
@RequiredArgsConstructor
public final class RunState {

  // Persisting a maximum of 6.000 ticks (5 minutes)
  private static final int MAX_RUN_TICKS = 5 * 60 * 20;

  private final ParkourContext context;
  private final PlayerState playerState;

  @Nullable
  private Vec3 startPosition = null;

  @Nullable
  private RunSplit endSplit = null;

  private List<RunSplit> splits = new ArrayList<>();
  private boolean runStarted = false;
  private boolean startedWithBurst = false;
  private boolean trackingEnabled = true;

  /// Tracks the elapsed ticks since the run started.
  private long timer = 0;

  /// Tracks the total ticks the player has been on the ground since the run started.
  private long groundTime = 0;

  /// Holds information for each passed tick in the run.
  private final LinkedList<RunTickState> tickStates = new LinkedList<>();

  /// Used to store the run as a macro and to render the different tick positions in a run.
  private final LinkedList<RunTickState> previousTickStates = new LinkedList<>();

  /// Records the ticks of a burst start. These ticks are part of the movement
  /// (macros, rendered tick states) but are not counted by the timer, matching
  /// how servers ignore sub-threshold movement.
  public void processBurstTicks(List<RunTickState> states) {
    if (states.isEmpty()) {
      return;
    }

    this.tickStates.addAll(states);
    this.startedWithBurst = true;
  }

  public void processTick(RunTickState state) {
    var lastTickOnGround = this.playerState.lastTick().onGround();

    if (lastTickOnGround && state.position().onGround()) {
      this.groundTime++;
    }

    this.timer++;

    if (this.trackingEnabled && this.tickStates.size() >= MAX_RUN_TICKS) {
      this.context.chat().display(
          Message.of("messages.run.tooLong").color(TextColorType.RED));
      this.trackingEnabled = false;
    }

    if (this.trackingEnabled) {
      this.tickStates.addLast(state);
    }
  }

  public void processFinish() {
    this.endSplit.updatePB(this.context, this.timer);
    this.runStarted = false;

    this.previousTickStates.clear();
    this.previousTickStates.addAll(this.tickStates);

    if (this.context.config().showRunFinishOffsets()) {
      this.showFinishOffsets();
    }
  }

  public void reset() {
    this.splits.forEach(split -> split.passed(false));
    this.runStarted = false;
    this.startedWithBurst = false;
    this.timer = 0;
    this.groundTime = 0;
    this.trackingEnabled = true;
    this.tickStates.clear();
  }

  private void showFinishOffsets() {
    var stringFormat = "%%.%df".formatted(this.context.config().chatDecimalPlaces());

    // Finish offset
    var lastTick = this.tickStates.getLast();
    var overlap = BoundingBoxUtils.computeOverlap(
        lastTick.position().playerBoundingBox(),
        this.endSplit.boundingBox());

    this.context.chat().display(
        Message.of("messages.run.finishOffsetHit")
            .color(TextColorType.GREEN)
            .arg(stringFormat.formatted(overlap.getX()), TextColorType.DARK_GREEN)
            .arg(stringFormat.formatted(overlap.getZ()), TextColorType.DARK_GREEN));

    // Missed tick offsets (3 max)
    for (var i = 1; i <= 3 && i < this.tickStates.size(); i++) {
      var tick = this.tickStates.get(this.tickStates.size() - 1 - i);
      var offset = BoundingBoxUtils.computeOverlap(
          tick.position().playerBoundingBox(),
          this.endSplit.boundingBox());
      var formattedTicks = TickFormatter.format(i, this.context.config().formatTicks());

      // Assuming that there is a block below the box, so the player must be above it to not collide.
      var isAboveBlock =
          tick.position().playerBoundingBox().getMinY() >= this.endSplit.boundingBox().getMinY();
      var possible = this.endSplit.triggerMode() == SplitBoxTriggerMode.IntersectXZAboveY
          ? isAboveBlock
          // Also check that we hit the split box if it is treated as a limited height.
          : isAboveBlock && offset.getY() > 0;

      // Not possible
      if (!possible) {
        break;
      }

      this.context.chat().display(
          Message.of("messages.run.finishOffsetMiss")
              .color(TextColorType.RED)
              .arg(formattedTicks, TextColorType.DARK_RED)
              .arg(stringFormat.formatted(offset.getX()), TextColorType.DARK_RED)
              .arg(stringFormat.formatted(offset.getZ()), TextColorType.DARK_RED));
    }
  }
}
