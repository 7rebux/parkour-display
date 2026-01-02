package pw.rebux.parkourdisplay.core.state;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public final class TickPosition {

  private double x = 0, y = 0, z = 0;
  private float yaw = 0, pitch = 0;
  private boolean onGround = false;
  private boolean movingForward = false;
  private boolean movingSideways = false;
}
