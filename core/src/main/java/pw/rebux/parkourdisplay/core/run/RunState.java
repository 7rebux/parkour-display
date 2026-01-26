package pw.rebux.parkourdisplay.core.run;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.core.run.split.RunSplit;
import pw.rebux.parkourdisplay.core.state.PositionOffset;
import pw.rebux.parkourdisplay.core.state.TickInput;

@Data
public class RunState {

  @Nullable
  private PositionOffset runStartPosition = null;

  @Nullable
  private RunSplit runEndSplit = null;

  private List<RunSplit> runSplits = new ArrayList<>();
  private boolean runStarted = false;
  private long runTimer = 0;
  private long runGroundTime = -1;
  private ArrayList<TickInput> runTickInputs = new ArrayList<>();

  public void reset() {
    this.runSplits.forEach(split -> split.passed(false));
    this.runStarted = false;
    this.runTimer = 0;
    this.runGroundTime = -1;
    this.runTickInputs.clear();
  }

  public boolean isRunSetUp() {
    return runStartPosition != null && runEndSplit != null;
  }
}
