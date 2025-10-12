package pw.rebux.parkourdisplay.core.state;

import lombok.Data;
import lombok.experimental.Accessors;
import net.labymod.api.util.collection.EvictingQueue;

@Data
@Accessors(fluent = true)
public class PlayerParkourState {

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

  // Persisting the last one minute of ticks
  private final EvictingQueue<TickInput> previousTicks =
      new EvictingQueue<>(60 * 20);
}
