package pw.rebux.parkourdisplay.core.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class TickPosition {

  private double x = 0, y = 0, z = 0;
  private float yaw = 0, pitch = 0;
  // We could work with a constant bounding box for the player, but there are states in which
  // the size can change, e.g., when the player is crouching or swimming.
  private AxisAlignedBoundingBox playerBoundingBox = new AxisAlignedBoundingBox();
  private boolean onGround = false;

  public DoubleVector3 toVector() {
    return new DoubleVector3(x, y, z);
  }
}
