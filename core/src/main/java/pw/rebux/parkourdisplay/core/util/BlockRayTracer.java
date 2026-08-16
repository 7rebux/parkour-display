package pw.rebux.parkourdisplay.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.labymod.api.util.math.MathHelper;
import net.labymod.api.util.math.vector.DoubleVector3;
import net.labymod.api.util.math.vector.IntVector3;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlockRayTracer {

  private static final double EPSILON = 1.0E-7;
  private static final int MAX_STEPS = 4096;

  public static @Nullable Hit trace(
      DoubleVector3 origin,
      DoubleVector3 direction,
      double maxDistance,
      ShapeProvider shapes
  ) {
    if (maxDistance <= 0) return null;

    double ox = origin.getX(), oy = origin.getY(), oz = origin.getZ();
    double dx = direction.getX(), dy = direction.getY(), dz = direction.getZ();
    double len = (float) direction.length();
    dx /= len; dy /= len; dz /= len;

    ox += dx * EPSILON;
    oy += dy * EPSILON;
    oz += dz * EPSILON;

    int x = MathHelper.floor(ox);
    int y = MathHelper.floor(oy);
    int z = MathHelper.floor(oz);

    int stepX = Double.compare(dx, 0) == 0 ? 0 : (dx > 0 ? 1 : -1);
    int stepY = Double.compare(dy, 0) == 0 ? 0 : (dy > 0 ? 1 : -1);
    int stepZ = Double.compare(dz, 0) == 0 ? 0 : (dz > 0 ? 1 : -1);

    // Ray distance between successive plane crossings on each axis.
    double tDeltaX = stepX == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dx);
    double tDeltaY = stepY == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dy);
    double tDeltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dz);

    // Ray distance to the first crossing on each axis.
    double tMaxX = stepX == 0 ? Double.POSITIVE_INFINITY
        : (stepX > 0 ? (x + 1 - ox) : (ox - x)) * tDeltaX;
    double tMaxY = stepY == 0 ? Double.POSITIVE_INFINITY
        : (stepY > 0 ? (y + 1 - oy) : (oy - y)) * tDeltaY;
    double tMaxZ = stepZ == 0 ? Double.POSITIVE_INFINITY
        : (stepZ > 0 ? (z + 1 - oz) : (oz - z)) * tDeltaZ;

    double t;
    var clip = new BoxClip();

    for (int i = 0; i < MAX_STEPS; i++) {
      double[] shape = shapes.shapeAt(x, y, z);
      if (shape != null && shape.length >= 6) {
        clipShape(ox, oy, oz, dx, dy, dz, x, y, z, shape, clip);
        if (!Double.isNaN(clip.t)) {
          if (clip.t > maxDistance) return null;
          return new Hit(
              new IntVector3(x, y, z),
              clip.t,
              new DoubleVector3(ox + dx * clip.t, oy + dy * clip.t, oz + dz * clip.t),
              clip.face
          );
        }
      }

      if (tMaxX < tMaxY && tMaxX < tMaxZ) {
        t = tMaxX;
        if (t > maxDistance) return null;
        x += stepX; tMaxX += tDeltaX;
      } else if (tMaxY < tMaxZ) {
        t = tMaxY;
        if (t > maxDistance) return null;
        y += stepY; tMaxY += tDeltaY;
      } else {
        t = tMaxZ;
        if (t > maxDistance) return null;
        z += stepZ; tMaxZ += tDeltaZ;
      }
    }
    return null;
  }

  /// Clips the ray against every box of one block's shape, keeping the nearest entry.
  private static void clipShape(
      double ox, double oy, double oz,
      double dx, double dy, double dz,
      int bx, int by, int bz,
      double[] shape,
      BoxClip out
  ) {
    out.t = Double.NaN;
    out.face = null;

    for (int i = 0; i + 5 < shape.length; i += 6) {
      clipBox(ox, oy, oz, dx, dy, dz,
          bx + shape[i], by + shape[i + 1], bz + shape[i + 2],
          bx + shape[i + 3], by + shape[i + 4], bz + shape[i + 5], out);
    }
  }

  private static void clipBox(
      double ox, double oy, double oz,
      double dx, double dy, double dz,
      double minX, double minY, double minZ,
      double maxX, double maxY, double maxZ,
      BoxClip out
  ) {
    double t0 = 0;
    double t1 = Double.POSITIVE_INFINITY;
    int axis = -1;

    if (dx != 0) {
      double inv = 1.0 / dx;
      double a = (minX - ox) * inv;
      double b = (maxX - ox) * inv;
      double lo = Math.min(a, b);
      double hi = Math.max(a, b);
      if (lo > t0) { t0 = lo; axis = 0; }
      if (hi < t1) t1 = hi;
    } else if (ox < minX || ox > maxX) {
      return;
    }

    if (dy != 0) {
      double inv = 1.0 / dy;
      double a = (minY - oy) * inv;
      double b = (maxY - oy) * inv;
      double lo = Math.min(a, b);
      double hi = Math.max(a, b);
      if (lo > t0) { t0 = lo; axis = 1; }
      if (hi < t1) t1 = hi;
    } else if (oy < minY || oy > maxY) {
      return;
    }

    if (dz != 0) {
      double inv = 1.0 / dz;
      double a = (minZ - oz) * inv;
      double b = (maxZ - oz) * inv;
      double lo = Math.min(a, b);
      double hi = Math.max(a, b);
      if (lo > t0) { t0 = lo; axis = 2; }
      if (hi < t1) t1 = hi;
    } else if (oz < minZ || oz > maxZ) {
      return;
    }

    if (t0 > t1) return; // Misses this box
    if (!Double.isNaN(out.t) && t0 >= out.t) return; // Farther than a box already found

    out.t = t0;
    out.face = switch (axis) {
      case 0 -> dx > 0 ? Face.WEST : Face.EAST;
      case 1 -> dy > 0 ? Face.DOWN : Face.UP;
      case 2 -> dz > 0 ? Face.NORTH : Face.SOUTH;
      default -> null; // Origin already inside the box
    };
  }

  /// Mutable result holder, reused across traversal steps.
  private static final class BoxClip {
    private double t = Double.NaN;
    private @Nullable Face face;
  }

  /// @param face the face the ray entered through, or null if the origin was already inside.
  public record Hit(
      IntVector3 location,
      double distance,
      DoubleVector3 hit,
      Face face
  ) {
  }

  @FunctionalInterface
  public interface ShapeProvider {
    /// @return null or an empty array for blocks the ray should pass through.
    double @Nullable [] shapeAt(int x, int y, int z);
  }

  @RequiredArgsConstructor
  public enum Face {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0);

    public final int dx, dy, dz;
  }
}
