package pw.rebux.parkourdisplay.core.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;

/// An immutable snapshot of a player's position and rotation at a specific tick.
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TickPosition {

  private double x, y, z;
  private double vx, vy, vz;
  private float yaw, pitch;
  // We could work with a constant bounding box for the player, but there are states in which
  // the size can change, e.g., when the player is crouching or swimming.
  private AxisAlignedBoundingBox playerBoundingBox;
  private boolean onGround;
  private boolean onClimbable;
  private boolean collided;

  public DoubleVector3 toVector() {
    return new DoubleVector3(x, y, z);
  }

  public static TickPosition of(ClientPlayer player) {
    double vx = player.position().getX() - player.previousPosition().getX();
    double vy = player.position().getY() - player.previousPosition().getY();
    double vz = player.position().getZ() - player.previousPosition().getZ();

    return TickPosition.builder()
        .x(player.position().getX())
        .y(player.position().getY())
        .z(player.position().getZ())
        .vx(vx)
        .vy(vy)
        .vz(vz)
        .yaw(player.getRotationYaw())
        .pitch(player.getRotationPitch())
        // Otherwise the reference would be stored
        .playerBoundingBox(player.axisAlignedBoundingBox().move(0, 0, 0))
        .onGround(player.isOnGround())
        .onClimbable(WorldUtils.onClimbable(player))
        // TODO: Is this reliable?
        .collided(player.getForwardMovingSpeed() > 0.0F && (vx == 0.0F || vz == 0.0F))
        .build();
  }

  public static TickPosition INITIAL = TickPosition.builder()
      .playerBoundingBox(new AxisAlignedBoundingBox())
      .build();
}
