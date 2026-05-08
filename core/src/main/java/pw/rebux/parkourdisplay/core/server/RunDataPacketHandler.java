package pw.rebux.parkourdisplay.core.server;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;
import net.labymod.serverapi.api.packet.PacketHandler;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.run.RunSplit;
import pw.rebux.parkourdisplay.integration.packets.RunDataPacket;

@RequiredArgsConstructor
public final class RunDataPacketHandler implements PacketHandler<RunDataPacket> {

  private final ParkourDisplayAddon addon;

  @Override
  public void handle(@NotNull UUID sender, @NonNull RunDataPacket packet) {
    var start = new DoubleVector3(packet.getStart().x(), packet.getStart().y(), packet.getStart().z());
    var endSplit = new RunSplit(
        "Finish",
        new AxisAlignedBoundingBox(
            packet.getMinEnd().x(), packet.getMinEnd().y(), packet.getMinEnd().z(),
            packet.getMaxEnd().x(), packet.getMaxEnd().y(), packet.getMaxEnd().z()
        ),
        packet.getTriggerMode()
    );
    endSplit.personalBest(packet.getPersonalBest());

    this.addon.runState().reset();
    this.addon.runState().splits().clear();
    this.addon.runState().startPosition(start);
    this.addon.runState().endSplit(endSplit);
  }
}
