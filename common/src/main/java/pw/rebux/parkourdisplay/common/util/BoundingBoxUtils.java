package pw.rebux.parkourdisplay.common.util;

import pw.rebux.parkourdisplay.common.geom.Aabb;
import pw.rebux.parkourdisplay.common.geom.Vec3;

public final class BoundingBoxUtils {

  private BoundingBoxUtils() {
  }

  public static boolean intersectsXZ(Aabb a, Aabb b) {
    return a.getMinX() < b.getMaxX() && a.getMaxX() > b.getMinX()
        && a.getMinZ() < b.getMaxZ() && a.getMaxZ() > b.getMinZ();
  }

  /// @param a First bounding box.
  /// @param b Second bounding box.
  /// @return true if the boxes intersect on the xz plane and b is above a.
  public static boolean intersectsXZAboveY(Aabb a, Aabb b) {
    return intersectsXZ(a, b) && b.getMinY() >= a.getMaxY();
  }

  public static boolean intersectsXZSameY(Aabb a, Aabb b) {
    return intersectsXZ(a, b) && a.getMaxY() == b.getMinY();
  }

  public static Vec3 computeOverlap(Aabb a, Aabb b) {
    return new Vec3(
        Math.min(a.getMaxX(), b.getMaxX()) - Math.max(a.getMinX(), b.getMinX()),
        Math.min(a.getMaxY(), b.getMaxY()) - Math.max(a.getMinY(), b.getMinY()),
        Math.min(a.getMaxZ(), b.getMaxZ()) - Math.max(a.getMinZ(), b.getMinZ())
    );
  }
}
