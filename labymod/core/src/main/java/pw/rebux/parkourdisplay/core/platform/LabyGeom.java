package pw.rebux.parkourdisplay.core.platform;

import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;
import pw.rebux.parkourdisplay.common.geom.Aabb;
import pw.rebux.parkourdisplay.common.geom.Vec3;

/// Converts between the platform-neutral geometry types used by the shared domain and LabyMod's
/// own math types, at the render/command boundaries where the domain meets LabyMod-specific code.
public final class LabyGeom {

  private LabyGeom() {
  }

  public static Vec3 toCommon(DoubleVector3 vector) {
    return new Vec3(vector.getX(), vector.getY(), vector.getZ());
  }

  public static DoubleVector3 toLaby(Vec3 vector) {
    return new DoubleVector3(vector.getX(), vector.getY(), vector.getZ());
  }

  public static Aabb toCommon(AxisAlignedBoundingBox box) {
    return new Aabb(
        box.getMinX(), box.getMinY(), box.getMinZ(),
        box.getMaxX(), box.getMaxY(), box.getMaxZ());
  }

  public static AxisAlignedBoundingBox toLaby(Aabb box) {
    return new AxisAlignedBoundingBox(
        box.getMinX(), box.getMinY(), box.getMinZ(),
        box.getMaxX(), box.getMaxY(), box.getMaxZ());
  }
}
