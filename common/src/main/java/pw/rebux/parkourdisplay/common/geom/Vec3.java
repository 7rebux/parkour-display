package pw.rebux.parkourdisplay.common.geom;

/// Platform-neutral immutable 3D vector. Field names (`x`, `y`, `z`) intentionally match
/// LabyMod's `DoubleVector3` so default Gson serialization stays byte-compatible with existing
/// saved data.
public final class Vec3 {

  private double x;
  private double y;
  private double z;

  public Vec3() {
  }

  public Vec3(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public double getX() {
    return this.x;
  }

  public double getY() {
    return this.y;
  }

  public double getZ() {
    return this.z;
  }

  public double distance(Vec3 other) {
    var dx = this.x - other.x;
    var dy = this.y - other.y;
    var dz = this.z - other.z;
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }
}
