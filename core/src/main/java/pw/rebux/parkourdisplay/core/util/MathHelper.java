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

  /// The distance between a point and the ray, or infinity if it lies behind the origin or farther
  /// than maxDistance along the ray. Expects a normalized direction.
  public static double rayPointDistance(
      DoubleVector3 origin,
      DoubleVector3 direction,
      double maxDistance,
      DoubleVector3 point
  ) {
    var toX = point.getX() - origin.getX();
    var toY = point.getY() - origin.getY();
    var toZ = point.getZ() - origin.getZ();
    var along = toX * direction.getX() + toY * direction.getY() + toZ * direction.getZ();

    if (along <= 0 || along > maxDistance) {
      return Double.POSITIVE_INFINITY;
    }

    var offX = toX - along * direction.getX();
    var offY = toY - along * direction.getY();
    var offZ = toZ - along * direction.getZ();
    return Math.sqrt(offX * offX + offY * offY + offZ * offZ);
  }
}
