package pw.rebux.parkourdisplay.core.util;

import java.util.List;
import net.labymod.api.Laby;
import net.labymod.api.client.Minecraft;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.util.math.Axis;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;

/// Replicates vanilla's collision resolution on top of the two primitives LabyMod exposes:
/// [net.labymod.api.client.world.ClientWorld#getBlockCollisions] and
/// [AxisAlignedBoundingBox#collide], which is a port of `VoxelShape#collide`.
public final class CollisionUtils {

  /// Distances below this are treated as no movement, see `VoxelShape#collideX`.
  private static final double EPSILON = 1.0E-7;

  /// Tolerance of `Mth#equal`, which vanilla uses to derive `horizontalCollision`.
  /// Note that 1.8.9 compares exactly instead.
  private static final double TOLERANCE = 1.0E-5;

  /// Movement used to probe for obstacles the player is already flush against.
  /// Small enough to only report contact, large enough to exceed [#TOLERANCE].
  private static final double PROBE_DISTANCE = 0.001;

  private static final Minecraft minecraft = Laby.labyAPI().minecraft();

  private CollisionUtils() {
  }

  /// Port of `Entity#collideBoundingBox` and `Entity#collideWithShapes`. Only block collisions
  /// are considered, vanilla additionally clips against entities and the world border. The
  /// step assist of `Entity#collide` is not replicated, so movement onto slabs and stairs is
  /// reported as clipped even though vanilla would step up.
  ///
  /// @param box    The box to move, usually the player bounding box.
  /// @param motion The movement to apply.
  /// @return The movement left after clipping it against the surrounding blocks.
  public static DoubleVector3 collide(AxisAlignedBoundingBox box, DoubleVector3 motion) {
    var world = minecraft.clientWorld();

    if (world == null) {
      return motion;
    }

    var colliders = world.getBlockCollisions(
        box.expandTowards(motion.getX(), motion.getY(), motion.getZ()));

    if (colliders.isEmpty()) {
      return motion;
    }

    var x = motion.getX();
    var y = motion.getY();
    var z = motion.getZ();

    if (y != 0) {
      y = clip(Axis.Y, box, colliders, y);

      if (y != 0) {
        box = box.move(0, y, 0);
      }
    }

    // Vanilla resolves the smaller horizontal component last.
    var zFirst = Math.abs(x) < Math.abs(z);

    if (zFirst && z != 0) {
      z = clip(Axis.Z, box, colliders, z);

      if (z != 0) {
        box = box.move(0, 0, z);
      }
    }

    if (x != 0) {
      x = clip(Axis.X, box, colliders, x);

      if (x != 0) {
        box = box.move(x, 0, 0);
      }
    }

    if (!zFirst && z != 0) {
      z = clip(Axis.Z, box, colliders, z);
    }

    return new DoubleVector3(x, y, z);
  }

  /// Port of the `horizontalCollision` assignment in `Entity#move`.
  ///
  /// @param wanted The movement that was requested.
  /// @param actual The movement left after [#collide].
  /// @return true if the movement was clipped on the xz plane.
  public static boolean collidedHorizontally(DoubleVector3 wanted, DoubleVector3 actual) {
    return !equal(wanted.getX(), actual.getX()) || !equal(wanted.getZ(), actual.getZ());
  }

  /// Approximates `horizontalCollision` without access to the delta movement, which the LabyMod
  /// API does not expose. Instead of the actual movement, a short probe along the movement input
  /// is clipped, so this reports whether the player is pushing into an obstacle it already
  /// touches. That is the steady state which triggers the ladder climb assist, but unlike vanilla
  /// it does not report a collision while the player is still approaching the obstacle.
  ///
  /// @param player The player to probe for.
  /// @return true if the player is pushing into an adjacent obstacle.
  public static boolean isPushingIntoObstacle(ClientPlayer player) {
    var probe = probeMotion(player);

    if (probe == null) {
      return false;
    }

    return collidedHorizontally(probe, collide(player.axisAlignedBoundingBox(), probe));
  }

  /// Builds the movement input in world space, see `Entity#getInputVector`.
  /// Only the direction is used, the length is normalized to [#PROBE_DISTANCE].
  private static DoubleVector3 probeMotion(ClientPlayer player) {
    var strafe = player.getStrafeMovingSpeed();
    var forward = player.getForwardMovingSpeed();
    var length = Math.hypot(strafe, forward);

    if (length < EPSILON) {
      return null;
    }

    var yaw = Math.toRadians(player.getRotationYaw());
    var sin = Math.sin(yaw);
    var cos = Math.cos(yaw);
    var scale = PROBE_DISTANCE / length;

    return new DoubleVector3(
        (strafe * cos - forward * sin) * scale,
        0,
        (forward * cos + strafe * sin) * scale
    );
  }

  private static double clip(
      Axis axis,
      AxisAlignedBoundingBox box,
      List<AxisAlignedBoundingBox> colliders,
      double distance
  ) {
    // AxisAlignedBoundingBox#collide omits this guard, VoxelShape#collideX has it.
    if (Math.abs(distance) < EPSILON) {
      return 0;
    }

    for (var collider : colliders) {
      distance = collider.collide(axis, box, distance);
    }

    return distance;
  }

  private static boolean equal(double a, double b) {
    return Math.abs(b - a) < TOLERANCE;
  }
}
