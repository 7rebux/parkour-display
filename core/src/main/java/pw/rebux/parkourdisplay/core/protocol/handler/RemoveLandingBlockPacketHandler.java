package pw.rebux.parkourdisplay.core.protocol.handler;

import com.mcpk.protocol.client.common.PacketHandler;
import com.mcpk.protocol.core.packet.clientbound.RemoveLandingBlockPacket;
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
