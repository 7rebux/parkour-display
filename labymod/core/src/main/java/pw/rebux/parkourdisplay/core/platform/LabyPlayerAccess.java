package pw.rebux.parkourdisplay.core.platform;

import net.labymod.api.client.entity.player.ClientPlayer;
import pw.rebux.parkourdisplay.common.geom.Aabb;
import pw.rebux.parkourdisplay.common.geom.Vec3;
import pw.rebux.parkourdisplay.common.platform.GameModeType;
import pw.rebux.parkourdisplay.common.platform.PlayerAccess;

/// LabyMod-backed [PlayerAccess], wrapping the current [ClientPlayer].
public final class LabyPlayerAccess implements PlayerAccess {

  private final ClientPlayer player;

  public LabyPlayerAccess(ClientPlayer player) {
    this.player = player;
  }

  @Override
  public Vec3 position() {
    var position = player.position();
    return new Vec3(position.getX(), position.getY(), position.getZ());
  }

  @Override
  public float yaw() {
    return player.getRotationYaw();
  }

  @Override
  public float pitch() {
    return player.getRotationPitch();
  }

  @Override
  public boolean onGround() {
    return player.isOnGround();
  }

  @Override
  public Aabb boundingBox() {
    // A fresh Aabb, so tick snapshots never retain a live reference to the player's box.
    return LabyGeom.toCommon(player.axisAlignedBoundingBox());
  }

  @Override
  public GameModeType gameMode() {
    return switch (player.gameMode()) {
      case SPECTATOR -> GameModeType.SPECTATOR;
      case CREATIVE -> GameModeType.CREATIVE;
      case ADVENTURE -> GameModeType.ADVENTURE;
      default -> GameModeType.SURVIVAL;
    };
  }

  @Override
  public void setRotationYaw(float yaw) {
    player.setRotationYaw(yaw);
  }

  @Override
  public void setRotationPitch(float pitch) {
    player.setRotationPitch(pitch);
  }

  @Override
  public void setPreviousRotationYaw(float yaw) {
    player.setPreviousRotationYaw(yaw);
  }

  @Override
  public void setPreviousRotationPitch(float pitch) {
    player.setPreviousRotationPitch(pitch);
  }
}
