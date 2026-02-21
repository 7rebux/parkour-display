package pw.rebux.parkourdisplay.core.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;

@Data
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TickPosition {

  private double x, y, z;
  private float yaw, pitch;
  // We could work with a constant bounding box for the player, but there are states in which
  // the size can change, e.g., when the player is crouching or swimming.
  private AxisAlignedBoundingBox playerBoundingBox;
  private boolean onGround;

  public DoubleVector3 toVector() {
    return new DoubleVector3(x, y, z);
  }

  public static TickPosition of(ClientPlayer player) {
    return TickPosition.builder()
        .x(player.position().getX())
        .y(player.position().getY())
        .z(player.position().getZ())
        .yaw(player.getRotationYaw())
        .pitch(player.getRotationPitch())
        // Otherwise the reference would be stored
        .playerBoundingBox(player.axisAlignedBoundingBox().move(0, 0, 0))
        .onGround(player.isOnGround())
        .build();
  }

  public static TickPosition INITIAL = TickPosition.builder()
      .x(0)
      .y(0)
      .z(0)
      .yaw(0)
      .pitch(0)
      .playerBoundingBox(new AxisAlignedBoundingBox())
      .onGround(false)
      .build();
}
