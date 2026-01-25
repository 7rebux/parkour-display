package pw.rebux.parkourdisplay.core.state;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.core.splits.RunSplit;

@Data
public final class PlayerParkourState {

  private double velocityX = 0, velocityY = 0, velocityZ = 0;
  private int jumpDuration = 0;
  private int groundDuration = 0;
  private double jumpX = 0, jumpY = 0, jumpZ = 0;
  private float jumpYaw = 0;
  private double landingX = 0, landingY = 0, landingZ = 0;
  private double hitX = 0, hitY = 0, hitZ = 0;
  private float hitYaw = 0;
  private double hitVelocityX = 0, hitVelocityZ = 0;
  private float lastFF = 0;
  private float lastTurn = 0;
  private String lastInput = "-";
  private String lastTiming = "-";

  private double lastTotalLandingBlockOffset = 0;
  private double lastLandingBlockOffsetX = 0, lastLandingBlockOffsetZ = 0;

  @Nullable private PositionOffset runStartPosition = null;
  @Nullable private RunSplit runEndSplit = null;
  private List<RunSplit> runSplits = new ArrayList<>();
  private boolean runStarted = false;
  private long runTimer = 0;
  private long runGroundTime = -1;
  private ArrayList<TickInput> runTickInputs = new ArrayList<>();

  public void resetRun() {
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
