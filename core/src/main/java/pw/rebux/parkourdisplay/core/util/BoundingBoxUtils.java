package pw.rebux.parkourdisplay.core.util;

import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;

public final class BoundingBoxUtils {

  public static boolean intersectsXZ(
      AxisAlignedBoundingBox a,
      AxisAlignedBoundingBox b
  ) {
    return a.getMinX() < b.getMaxX() && a.getMaxX() > b.getMinX() &&
        a.getMinZ() < b.getMaxZ() && a.getMaxZ() > b.getMinZ();
  }

  /// @param a First bounding box.
  /// @param b Second bounding box.
  /// @return true if the boxes intersect on the xz plane and b is above a.
  public static boolean intersectsXZAboveY(
      AxisAlignedBoundingBox a,
      AxisAlignedBoundingBox b
  ) {
    return intersectsXZ(a, b) && b.getMinY() >= a.getMaxY();
  }

  public static boolean intersectsXZSameY(
      AxisAlignedBoundingBox a,
      AxisAlignedBoundingBox b
  ) {
    return intersectsXZ(a, b) && a.getMaxY() == b.getMinY();
  }

  public static DoubleVector3 computeOverlap(
      AxisAlignedBoundingBox a,
      AxisAlignedBoundingBox b
  ) {
    return new DoubleVector3(
        Math.min(a.getMaxX(), b.getMaxX()) -
            Math.max(a.getMinX(), b.getMinX()),

        Math.min(a.getMaxY(), b.getMaxY()) -
            Math.max(a.getMinY(), b.getMinY()),

        Math.min(a.getMaxZ(), b.getMaxZ()) -
            Math.max(a.getMinZ(), b.getMinZ())
    );
  }
}
