package pw.rebux.parkourdisplay.integration;

import com.google.auto.service.AutoService;
import net.labymod.serverapi.api.packet.Direction;
import net.labymod.serverapi.core.AbstractLabyModPlayer;
import net.labymod.serverapi.core.AbstractLabyModProtocolService;
import net.labymod.serverapi.core.AddonProtocol;
import net.labymod.serverapi.core.integration.LabyModIntegrationPlayer;
import net.labymod.serverapi.core.integration.LabyModProtocolIntegration;
import net.labymod.serverapi.core.model.moderation.Permission;
import net.labymod.serverapi.core.model.moderation.RecommendedAddon;
import org.jspecify.annotations.NonNull;
import pw.rebux.parkourdisplay.api.Permissions;
import pw.rebux.parkourdisplay.integration.packets.RunDataPacket;

// TODO: The identifier of the protocol labymod:parkourdisplay is longer than 20 characters. This will cause issues with 1.8 & 1.12.2 users
@SuppressWarnings("unused")
@AutoService(LabyModProtocolIntegration.class)
public final class ParkourDisplayIntegration implements LabyModProtocolIntegration {

  public static final String NAMESPACE = "parkourdisplay";
  public static final Permission MACRO_PERMISSION = Permission.of(Permissions.RUN_MACROS);
  public static final RecommendedAddon RECOMMENDED_ADDON = RecommendedAddon.of(NAMESPACE, false);

  private AbstractLabyModProtocolService protocolService;
  private AddonProtocol addonProtocol;

  @Override
  public void initialize(AbstractLabyModProtocolService protocolService) {
    if (this.protocolService != null) {
      throw new IllegalStateException("ParkourDisplayIntegration is already initialized");
    }

    this.protocolService = protocolService;

    this.addonProtocol = new AddonProtocol(protocolService, NAMESPACE);
    this.addonProtocol.registerPacket(0, RunDataPacket.class, Direction.CLIENTBOUND);

    protocolService.registry().registerProtocol(this.addonProtocol);
  }

  @Override
  public LabyModIntegrationPlayer createIntegrationPlayer(AbstractLabyModPlayer<?> labyModPlayer) {
    return new ParkourDisplayPlayer(this.addonProtocol, labyModPlayer.getUniqueId());
  }

  public @NonNull AddonProtocol parkourDisplayProtocol() {
    if (this.addonProtocol == null) {
      throw new IllegalStateException("ParkourDisplayIntegration is not initialized yet");
    }

    return this.addonProtocol;
  }
}
