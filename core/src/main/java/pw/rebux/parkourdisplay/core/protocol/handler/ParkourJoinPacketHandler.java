package pw.rebux.parkourdisplay.core.protocol.handler;

import org.mcpk.protocol.client.common.PacketHandler;
import org.mcpk.protocol.core.model.DoubleVec3;
import org.mcpk.protocol.core.model.Split;
import org.mcpk.protocol.core.packet.clientbound.ParkourJoinPacket;
import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.gui.screen.activity.activities.ConfirmActivity;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.ParkourDisplayConfiguration.ServerDataLoadMode;
import pw.rebux.parkourdisplay.core.run.RunSplit;
import pw.rebux.parkourdisplay.core.run.SplitBoxTriggerMode;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

/// Loads the run (start, splits, finish and personal bests) a server sends when the player joins a parkour.
@RequiredArgsConstructor
public final class ParkourJoinPacketHandler implements PacketHandler<ParkourJoinPacket> {

  private final ParkourDisplayAddon addon;

  @Override
  public void handle(ParkourJoinPacket packet) {
    var mode = this.addon.configuration().serverDataLoadMode().get();

    if (mode == ServerDataLoadMode.Always) {
      this.loadRunData(packet);
    } else if (mode == ServerDataLoadMode.Confirm) {
      ConfirmActivity.confirm(
          Component.translatable("parkourdisplay.activities.loadRunData.title"),
          success -> {
            if (success) {
              this.loadRunData(packet);
            }
          }
      );
    }
  }

  private void loadRunData(ParkourJoinPacket packet) {
    var endSplit = toRunSplit(packet.getFinish(), "Finish");

    this.addon.runState().reset();
    var splits = this.addon.runState().splits();
    splits.clear();
    for (var split : packet.getSplits()) {
      splits.add(toRunSplit(split, "Split %d".formatted(splits.size() + 1)));
    }
    this.addon.runState().startPosition(toVector(packet.getStartPosition()));
    this.addon.runState().endSplit(endSplit);

    ChatMessage.of("messages.run.loadedFromServer")
        .withColor(NamedTextColor.GREEN)
        .withArgs(Component.text(packet.getName()))
        .send();
  }

  /// Uses the server's name for the split if it sent one, otherwise the given default label.
  private static RunSplit toRunSplit(Split split, String defaultLabel) {
    var box = split.getBox();
    var runSplit = new RunSplit(
        split.getName() != null ? split.getName() : defaultLabel,
        new AxisAlignedBoundingBox(
            box.getMin().getX(), box.getMin().getY(), box.getMin().getZ(),
            box.getMax().getX(), box.getMax().getY(), box.getMax().getZ()
        ),
        SplitBoxTriggerMode.valueOf(split.getIntersectionType().name())
    );
    runSplit.personalBest(split.getPersonalBest());
    return runSplit;
  }

  private static DoubleVector3 toVector(DoubleVec3 vec) {
    return new DoubleVector3(vec.getX(), vec.getY(), vec.getZ());
  }
}
