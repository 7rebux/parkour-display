package pw.rebux.parkourdisplay.core.protocol;

import com.mcpk.protocol.client.common.AbstractClientParkourProtocolService;
import com.mcpk.protocol.core.Packet;
import com.mcpk.protocol.core.ParkourProtocol;
import com.mcpk.protocol.core.packet.ClientLoginPacket;
import com.mcpk.protocol.core.packet.GrantPermissionPacket;
import com.mcpk.protocol.core.packet.RevokePermissionPacket;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.labymod.api.Laby;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.network.server.ServerJoinEvent;
import net.labymod.api.event.client.network.server.SubServerSwitchEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

/// LabyMod transport for the [Parkour Protocol](https://github.com/minecraft-parkour/parkour-protocol).
///
/// Owns the `parkour:protocol` plugin channel and performs the login handshake whenever the client
/// joins a server or switches sub-servers behind a proxy. Features register their packet handlers
/// through [#registerHandler].
@RequiredArgsConstructor
public final class ParkourProtocolClient extends AbstractClientParkourProtocolService {

  public static final ResourceLocation CHANNEL =
      ResourceLocation.parse(ParkourProtocol.MESSENGER_CHANNEL);

  private final ParkourDisplayAddon addon;

  /// Permissions granted by the current server. Reset whenever the server changes.
  private final Set<String> permissions = new HashSet<>();

  /// Registers the plugin channel, so it is announced to servers via `minecraft:register`.
  /// Servers (e.g. Bukkit) only send plugin messages on channels the client registered.
  public void initialize() {
    Laby.references().payloadRegistry().registerPayloadChannel(CHANNEL);

    this.registerHandler(GrantPermissionPacket.class,
        packet -> this.permissions.add(packet.getPermission()));
    this.registerHandler(RevokePermissionPacket.class,
        packet -> this.permissions.remove(packet.getPermission()));
  }

  /// @return whether the current server granted the given permission
  public boolean hasPermission(String permission) {
    return this.permissions.contains(permission);
  }

  /// Serializes the given packet and sends it to the current server.
  ///
  /// @param packet the packet to send; must be a client-to-server packet
  /// @throws IllegalArgumentException if the packet is not meant to be sent by clients
  @Override
  public void sendPacket(Packet packet) {
    this.ensurePacketFlow(packet);

    var serverController = this.addon.labyAPI().serverController();
    if (!serverController.isConnected()) {
      return;
    }

    serverController.sendPayload(CHANNEL, this.protocol.serialize(packet));
  }

  @Subscribe
  public void onServerJoin(ServerJoinEvent event) {
    this.login();
  }

  @Subscribe
  public void onSubServerSwitch(SubServerSwitchEvent event) {
    // Each backend server behind a proxy tracks the handshake separately.
    this.login();
  }

  @Subscribe
  public void onServerDisconnect(ServerDisconnectEvent event) {
    this.permissions.clear();
  }

  @Subscribe
  public void onPayload(NetworkPayloadEvent event) {
    if (event.side() != NetworkPayloadEvent.Side.RECEIVE || !isChannel(event.identifier())) {
      return;
    }

    Packet packet;
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

  private void login() {
    this.permissions.clear();
    this.sendPacket(new ClientLoginPacket(ParkourProtocol.VERSION));
  }

  private static boolean isChannel(ResourceLocation location) {
    return location != null
        && CHANNEL.getNamespace().equals(location.getNamespace())
        && CHANNEL.getPath().equals(location.getPath());
  }
}
