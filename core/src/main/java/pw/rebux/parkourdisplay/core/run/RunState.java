package pw.rebux.parkourdisplay.core.run;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.labymod.api.util.math.vector.DoubleVector3;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.run.split.Split;
import pw.rebux.parkourdisplay.core.run.split.SplitBoxTriggerMode;
import pw.rebux.parkourdisplay.core.util.BoundingBoxUtils;
import pw.rebux.parkourdisplay.core.util.ChatMessage;
import pw.rebux.parkourdisplay.core.util.TickFormatter;

@Data
@RequiredArgsConstructor
public final class RunState {

  // Persisting a maximum of 6.000 ticks (5 minutes)
  private static final int MAX_RUN_TICKS = 5 * 60 * 20;

  private final ParkourDisplayAddon addon;

  @Nullable
  private DoubleVector3 startPosition = null;

  @Nullable
  private Split endSplit = null;

  private List<Split> splits = new ArrayList<>();
  private boolean runStarted = false;
  private boolean trackingEnabled = true;

  /// Tracks the elapsed ticks since the run started.
  private long timer = 0;

  /// Tracks the total ticks the player has been on the ground since the run started.
  private long groundTime = 0;

  /// Holds information for each passed tick in the run.
  private final LinkedList<RunTickState> tickStates = new LinkedList<>();

  /// Used to store the run as a macro and to render the different tick positions in a run.
  private final LinkedList<RunTickState> previousTickStates = new LinkedList<>();

  public void processTick(RunTickState state) {
    var lastTickOnGround = this.addon.playerState().lastTick().onGround();

    if (lastTickOnGround && state.position().onGround()) {
      this.groundTime++;
    }

    this.timer++;

    if (this.trackingEnabled && this.tickStates.size() >= MAX_RUN_TICKS) {
      ChatMessage.ofTranslatable("messages.run.tooLong").send();
      this.trackingEnabled = false;
    }

    if (this.trackingEnabled) {
      this.tickStates.addLast(state);
    }
  }

  public void processFinish() {
    this.endSplit.updatePB(this.addon, this.timer);
    this.runStarted = false;

    this.previousTickStates.clear();
    this.previousTickStates.addAll(this.tickStates);

    if (this.addon.configuration().showRunFinishOffsets().get()) {
      this.showFinishOffsets();
    }
  }

  public void reset() {
    this.splits.forEach(split -> split.passed(false));
    this.runStarted = false;
    this.timer = 0;
    this.groundTime = 0;
    this.trackingEnabled = true;
    this.tickStates.clear();
  }

  private void showFinishOffsets() {
    var stringFormat = "%%.%df".formatted(this.addon.configuration().offsetDecimalPlaces().get());

    // Finish offset
    var lastTick = this.tickStates.getLast();
    var overlap = BoundingBoxUtils.computeOverlap(
        lastTick.position().playerBoundingBox(),
        this.endSplit.boundingBox());

    ChatMessage.ofTranslatable("messages.run.finishOffsetHit")
        .withArgs(stringFormat.formatted(overlap.getX()), stringFormat.formatted(overlap.getZ()))
        .send();

    // Missed tick offsets (3 max)
    for (var i = 1; i <= 3 && i < this.tickStates.size(); i++) {
      var tick = this.tickStates.get(this.tickStates.size() - 1 - i);
      var offset = BoundingBoxUtils.computeOverlap(
          tick.position().playerBoundingBox(),
          this.endSplit.boundingBox());
      var formattedTicks = TickFormatter.format(i, this.addon.configuration().formatTicks().get());

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

      ChatMessage.ofTranslatable("messages.run.finishOffsetMiss")
          .withArgs(formattedTicks, stringFormat.formatted(offset.getX()), stringFormat.formatted(offset.getZ()))
          .send();
    }
  }
}
