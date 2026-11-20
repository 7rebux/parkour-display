package pw.rebux.parkourdisplay.core.protocol.handler;

import com.mcpk.protocol.client.common.PacketHandler;
import com.mcpk.protocol.core.packet.clientbound.AddLandingBlockPacket;
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
