package pw.rebux.parkourdisplay.core.protocol.handler;

import org.mcpk.protocol.client.common.PacketHandler;
import org.mcpk.protocol.core.packet.clientbound.RemoveLandingBlockPacket;
import lombok.RequiredArgsConstructor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@RequiredArgsConstructor
public final class RemoveLandingBlockPacketHandler implements PacketHandler<RemoveLandingBlockPacket> {

  private final ParkourDisplayAddon addon;

  @Override
  public void handle(RemoveLandingBlockPacket packet) {
    throw new UnsupportedOperationException();
  }
}
