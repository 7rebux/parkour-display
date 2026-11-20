package pw.rebux.parkourdisplay.core.protocol;

import com.mcpk.protocol.client.common.AbstractClientParkourProtocolService;
import com.mcpk.protocol.client.common.ClientSession;
import com.mcpk.protocol.core.ParkourProtocol;
import com.mcpk.protocol.core.ProtocolLogger;
import com.mcpk.protocol.core.packet.WirePacket;
import com.mcpk.protocol.core.packet.clientbound.AddLandingBlockPacket;
import com.mcpk.protocol.core.packet.clientbound.ParkourJoinPacket;
import com.mcpk.protocol.core.packet.clientbound.RemoveLandingBlockPacket;
import lombok.RequiredArgsConstructor;
import net.labymod.api.Laby;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.protocol.handler.AddLandingBlockPacketHandler;
import pw.rebux.parkourdisplay.core.protocol.handler.ParkourJoinPacketHandler;
import pw.rebux.parkourdisplay.core.protocol.handler.RemoveLandingBlockPacketHandler;

/// LabyMod transport for parkour-protocol
@RequiredArgsConstructor
public final class ParkourProtocolService extends AbstractClientParkourProtocolService {

  private static final ResourceLocation CHANNEL =
      ResourceLocation.parse(ParkourProtocol.MESSENGER_CHANNEL);

  private final ParkourDisplayAddon addon;

  public void initialize() {
    Laby.references().payloadRegistry().registerPayloadChannel(CHANNEL);

    this.registerHandler(ParkourJoinPacket.class, new ParkourJoinPacketHandler(this.addon));
    this.registerHandler(AddLandingBlockPacket.class, new AddLandingBlockPacketHandler(this.addon));
    this.registerHandler(RemoveLandingBlockPacket.class, new RemoveLandingBlockPacketHandler(this.addon));
  }

  @Override
  protected ProtocolLogger logger() {
    return new ParkourProtocolLogger(this.addon);
  }

  @Override
  protected void onSessionStart(ClientSession session) {
    this.addon.logger().info("Server speaks parkour protocol");
  }

  @Override
  protected void dispatch(WirePacket packet) {
    var serverController = this.addon.labyAPI().serverController();
    if (!serverController.isConnected()) {
      return;
    }

    serverController.sendPayload(CHANNEL, this.protocol.serialize(packet));
  }

  @Subscribe
  public void onServerDisconnect(ServerDisconnectEvent event) {
    this.addon.labyAPI().minecraft().executeOnRenderThread(this::endSession);
  }

  @Subscribe
  public void onPayload(NetworkPayloadEvent event) {
    if (event.side() != NetworkPayloadEvent.Side.RECEIVE || !isChannel(event.identifier())) {
      return;
    }

    WirePacket packet;
    try {
      packet = this.protocol.deserialize(event.getPayload());
    } catch (Exception e) {
      this.addon.logger().warn("Failed to deserialize incoming parkour protocol packet", e);
      return;
    }

    // Payloads may arrive on the network thread; handlers touch game state.
    this.addon.labyAPI().minecraft().executeOnRenderThread(() -> {
      try {
        this.handleIncoming(packet);
      } catch (Exception e) {
        this.addon.logger().warn("Failed to handle parkour protocol packet {}",
            packet.getClass().getSimpleName(), e);
      }
    });
  }

  private static boolean isChannel(ResourceLocation location) {
    return location != null
        && CHANNEL.getNamespace().equals(location.getNamespace())
        && CHANNEL.getPath().equals(location.getPath());
  }
}
