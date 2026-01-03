package pw.rebux.parkourdisplay.core.state;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import net.labymod.api.util.collection.EvictingQueue;
import pw.rebux.parkourdisplay.core.splits.RunSplit;

@Data
@Accessors(fluent = true)
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

  private PositionOffset runStartPosition = null;
  private RunSplit runEndSplit = null;
  private List<RunSplit> runSplits = new ArrayList<>();
  // Persisting a maximum of 6.000 ticks (5 minutes)
  // TODO: Doesnt make too much sense as this is only used to save macros right now and the macro would then be completely useless
  private EvictingQueue<TickInput> runTickInputs = new EvictingQueue<>(5 * 60 * 20);
  private int runGroundTime = 0;
  private boolean runStarted = false;

  public boolean isRunSetUp() {
    return runStartPosition != null && runEndSplit != null;
  }
}
