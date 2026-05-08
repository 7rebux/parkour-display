package pw.rebux.parkourdisplay.integration;

import java.util.UUID;
import net.labymod.serverapi.core.AddonProtocol;
import net.labymod.serverapi.core.integration.LabyModIntegrationPlayer;
import net.labymod.serverapi.core.packet.clientbound.game.moderation.PermissionPacket;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.api.SplitBoxTriggerMode;
import pw.rebux.parkourdisplay.integration.model.DoubleVec3;
import pw.rebux.parkourdisplay.integration.packets.RunDataPacket;

@SuppressWarnings("unused")
public class ParkourDisplayPlayer implements LabyModIntegrationPlayer {

  private final AddonProtocol addonProtocol;
  private final UUID uuid;

  public ParkourDisplayPlayer(AddonProtocol addonProtocol, UUID uuid) {
    this.addonProtocol = addonProtocol;
    this.uuid = uuid;
  }

  public void loadRun(
      @NonNull String name,
      @NonNull DoubleVec3 start,
      @NonNull DoubleVec3 minEnd,
      @NonNull DoubleVec3 maxEnd,
      @NonNull SplitBoxTriggerMode triggerMode,
      @Nullable Long personalBest
  ) {
    var packet = new RunDataPacket(name, start, minEnd, maxEnd, triggerMode, personalBest);
    this.addonProtocol.sendPacket(this.uuid, packet);
  }

  public void allowMacros() {
    var packet = new PermissionPacket(ParkourDisplayIntegration.MACRO_PERMISSION.allow());
    this.addonProtocol.sendPacket(this.uuid, packet);
  }

  public void denyMacros() {
    var packet = new PermissionPacket(ParkourDisplayIntegration.MACRO_PERMISSION.deny());
    this.addonProtocol.sendPacket(this.uuid, packet);
  }

  public UUID getUuid() {
    return uuid;
  }

  @Override
  public String toString() {
    return "ParkourDisplayPlayer{uuid=" + uuid + "}";
  }
}
