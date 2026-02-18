package pw.rebux.parkourdisplay.core.util;

import net.labymod.api.util.math.vector.DoubleVector3;

public final class MathHelper {

  public static float formatYaw(float yaw) {
    float facing = yaw % 360;
    if (facing > 180) facing -= 360;
    else if (facing < -180) facing += 360;
    return facing;
  }

  public static double offsetDistance(DoubleVector3 offset) {
    if (offset.getX() <= 0 && offset.getZ() <= 0) {
      return -Math.hypot(offset.getX(), offset.getZ());
    } else if (offset.getX() <= 0) {
      return offset.getX();
    } else if (offset.getZ() <= 0) {
      return offset.getZ();
    } else {
      return Math.hypot(offset.getX(), offset.getZ());
    }
  }
}
