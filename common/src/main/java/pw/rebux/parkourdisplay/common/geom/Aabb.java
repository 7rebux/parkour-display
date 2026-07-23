package pw.rebux.parkourdisplay.common.geom;

/// Platform-neutral axis-aligned bounding box. Field names (`minX`…`maxZ`) intentionally match
/// LabyMod's `AxisAlignedBoundingBox` so default Gson serialization stays byte-compatible with
/// existing saved splits.
public final class Aabb {

  private double minX;
  private double minY;
  private double minZ;
  private double maxX;
  private double maxY;
  private double maxZ;

  public Aabb() {
  }

  public Aabb(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    this.minX = minX;
    this.minY = minY;
    this.minZ = minZ;
    this.maxX = maxX;
    this.maxY = maxY;
    this.maxZ = maxZ;
  }

  public double getMinX() {
    return this.minX;
  }

  public double getMinY() {
    return this.minY;
  }

  public double getMinZ() {
    return this.minZ;
  }

  public double getMaxX() {
    return this.maxX;
  }

  public double getMaxY() {
    return this.maxY;
  }

  public double getMaxZ() {
    return this.maxZ;
  }

  /// @return a new box translated by the given offset (the original is left untouched, matching
  /// how the LabyMod API's `move` is used to snapshot immutable tick positions).
  public Aabb move(double dx, double dy, double dz) {
    return new Aabb(
        this.minX + dx, this.minY + dy, this.minZ + dz,
        this.maxX + dx, this.maxY + dy, this.maxZ + dz);
  }

  public Aabb move(Vec3 offset) {
    return this.move(offset.getX(), offset.getY(), offset.getZ());
  }

  public boolean intersects(Aabb other) {
    return this.minX < other.maxX && this.maxX > other.minX
        && this.minY < other.maxY && this.maxY > other.minY
        && this.minZ < other.maxZ && this.maxZ > other.minZ;
  }
}
