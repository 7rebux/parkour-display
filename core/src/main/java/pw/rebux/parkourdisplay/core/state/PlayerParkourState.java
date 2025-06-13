package pw.rebux.parkourdisplay.core.state;

import lombok.Data;
import lombok.experimental.Accessors;

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
}
