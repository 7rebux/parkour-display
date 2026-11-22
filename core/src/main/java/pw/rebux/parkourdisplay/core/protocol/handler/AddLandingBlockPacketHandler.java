package pw.rebux.parkourdisplay.core.protocol.handler;

import org.mcpk.protocol.client.common.PacketHandler;
import org.mcpk.protocol.core.packet.clientbound.AddLandingBlockPacket;
import lombok.RequiredArgsConstructor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@RequiredArgsConstructor
public final class AddLandingBlockPacketHandler implements PacketHandler<AddLandingBlockPacket> {

  private final ParkourDisplayAddon addon;

  @Override
  public void handle(AddLandingBlockPacket packet) {
    throw new UnsupportedOperationException();
  }
}
