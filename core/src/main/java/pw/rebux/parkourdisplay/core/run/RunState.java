package pw.rebux.parkourdisplay.core.run;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.run.split.RunSplit;

@Data
@RequiredArgsConstructor
public class RunState {

  // Persisting a maximum of 6.000 ticks (5 minutes)
  private static final int MAX_RUN_TICKS = 5 * 60 * 20;
  private static final Component RUN_TOO_LONG_MESSAGE =
      Component.text("Run too long, no longer tracking tick states.", NamedTextColor.RED);

  private final ParkourDisplayAddon addon;

  @Nullable private PositionOffset runStartPosition = null;
  @Nullable private RunSplit runEndSplit = null;
  private List<RunSplit> runSplits = new ArrayList<>();
  private boolean runStarted = false;
  private boolean trackingEnabled = true;

  /// Tracks the elapsed ticks since the run started.
  private long timer = 0;

  /// Tracks the total ticks the player has been on the ground since the run started.
  private long runGroundTime = -1;

  /// Holds information for each passed tick in the run.
  private final LinkedList<RunTickState> tickStates = new LinkedList<>();

  /// Used to store the run as a macro and to render the different tick positions in a run.
  private final LinkedList<RunTickState> previousTickStates = new LinkedList<>();

  public void processTick(RunTickState state) {
    var lastTick = tickStates.peekLast();

    if (lastTick == null || lastTick.position().onGround() && state.position().onGround()) {
      runGroundTime++;
    }

    timer++;

    if (tickStates.size() >= MAX_RUN_TICKS) {
      this.addon.displayMessage(RUN_TOO_LONG_MESSAGE);
      trackingEnabled = false;
    }

    if (trackingEnabled) {
      tickStates.addLast(state);
    }
  }

  public void processFinish() {
    if (runEndSplit == null) {
      return;
    }

    runEndSplit.updatePB(addon, timer);
    runStarted = false;

    previousTickStates.clear();
    previousTickStates.addAll(tickStates);
  }

  public void reset() {
    this.runSplits.forEach(split -> split.passed(false));
    this.runStarted = false;
    this.timer = 0;
    this.runGroundTime = -1;
    this.trackingEnabled = true;
    this.tickStates.clear();
  }
}
