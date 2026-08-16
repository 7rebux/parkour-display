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

  /// Distance from `point` to the closest point on `box`, or 0 if `point` lies inside it.
  public static double distanceToPoint(
      AxisAlignedBoundingBox box,
      DoubleVector3 point
  ) {
    var dx = Math.max(Math.max(box.getMinX() - point.getX(), point.getX() - box.getMaxX()), 0);
    var dy = Math.max(Math.max(box.getMinY() - point.getY(), point.getY() - box.getMaxY()), 0);
    var dz = Math.max(Math.max(box.getMinZ() - point.getZ(), point.getZ() - box.getMaxZ()), 0);

    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  /// Area of the overlap between the two boxes on the xz plane, or 0 if they do not overlap.
  public static double overlapAreaXZ(
      AxisAlignedBoundingBox a,
      AxisAlignedBoundingBox b
  ) {
    var overlapX = Math.min(a.getMaxX(), b.getMaxX()) - Math.max(a.getMinX(), b.getMinX());
    var overlapZ = Math.min(a.getMaxZ(), b.getMaxZ()) - Math.max(a.getMinZ(), b.getMinZ());

    if (overlapX <= 0 || overlapZ <= 0) return 0;

    return overlapX * overlapZ;
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
