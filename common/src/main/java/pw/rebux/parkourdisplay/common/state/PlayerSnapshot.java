package pw.rebux.parkourdisplay.common.state;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pw.rebux.parkourdisplay.common.geom.Aabb;
import pw.rebux.parkourdisplay.common.geom.Vec3;
import pw.rebux.parkourdisplay.common.platform.PlayerAccess;

/// An immutable snapshot of a player's position and rotation at a specific tick.
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PlayerSnapshot {

  private double x, y, z;
  private float yaw, pitch;
  // We could work with a constant bounding box for the player, but there are states in which
  // the size can change, e.g., when the player is crouching or swimming.
  private Aabb playerBoundingBox;
  private boolean onGround;

  public Vec3 toVector() {
    return new Vec3(x, y, z);
  }

  public static PlayerSnapshot of(PlayerAccess player) {
    return PlayerSnapshot.builder()
        .x(player.position().getX())
        .y(player.position().getY())
        .z(player.position().getZ())
        .yaw(player.yaw())
        .pitch(player.pitch())
        // The adapter already hands back a copy, so no live reference is stored.
        .playerBoundingBox(player.boundingBox())
        .onGround(player.onGround())
        .build();
  }

  public static PlayerSnapshot INITIAL = PlayerSnapshot.builder()
      .x(0)
      .y(0)
      .z(0)
      .yaw(0)
      .pitch(0)
      .playerBoundingBox(new Aabb())
      .onGround(false)
      .build();
}
