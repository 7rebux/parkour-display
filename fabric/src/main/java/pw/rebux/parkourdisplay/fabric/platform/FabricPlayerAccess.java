package pw.rebux.parkourdisplay.fabric.platform;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.world.GameMode;
import pw.rebux.parkourdisplay.common.geom.Aabb;
import pw.rebux.parkourdisplay.common.geom.Vec3;
import pw.rebux.parkourdisplay.common.platform.GameModeType;
import pw.rebux.parkourdisplay.common.platform.PlayerAccess;

/// Fabric-backed [PlayerAccess], wrapping the local [ClientPlayerEntity].
public final class FabricPlayerAccess implements PlayerAccess {

  private final ClientPlayerEntity player;
  private final GameMode gameMode;

  public FabricPlayerAccess(ClientPlayerEntity player, GameMode gameMode) {
    this.player = player;
    this.gameMode = gameMode;
  }

  @Override
  public Vec3 position() {
    var pos = player.getPos();
    return new Vec3(pos.x, pos.y, pos.z);
  }

  @Override
  public float yaw() {
    return player.getYaw();
  }

  @Override
  public float pitch() {
    return player.getPitch();
  }

  @Override
  public boolean onGround() {
    return player.isOnGround();
  }

  @Override
  public Aabb boundingBox() {
    var box = player.getBoundingBox();
    return new Aabb(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
  }

  @Override
  public GameModeType gameMode() {
    if (this.gameMode == null) {
      return GameModeType.SURVIVAL;
    }
    return switch (this.gameMode) {
      case SPECTATOR -> GameModeType.SPECTATOR;
      case CREATIVE -> GameModeType.CREATIVE;
      case ADVENTURE -> GameModeType.ADVENTURE;
      default -> GameModeType.SURVIVAL;
    };
  }

  @Override
  public void setRotationYaw(float yaw) {
    player.setYaw(yaw);
  }

  @Override
  public void setRotationPitch(float pitch) {
    player.setPitch(pitch);
  }

  @Override
  public void setPreviousRotationYaw(float yaw) {
    player.lastYaw = yaw;
  }

  @Override
  public void setPreviousRotationPitch(float pitch) {
    player.lastPitch = pitch;
  }
}
