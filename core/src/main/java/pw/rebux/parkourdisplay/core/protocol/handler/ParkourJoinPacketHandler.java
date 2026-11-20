package pw.rebux.parkourdisplay.core.protocol.handler;

import com.mcpk.protocol.client.common.PacketHandler;
import com.mcpk.protocol.core.model.DoubleVec3;
import com.mcpk.protocol.core.packet.clientbound.ParkourJoinPacket;
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

/// Loads the run (start, finish and personal best) a server sends when the player joins a parkour.
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
    var finish = packet.getFinish();
    var endSplit = new RunSplit(
        "Finish",
        new AxisAlignedBoundingBox(
            finish.getMin().getX(), finish.getMin().getY(), finish.getMin().getZ(),
            finish.getMax().getX(), finish.getMax().getY(), finish.getMax().getZ()
        ),
        SplitBoxTriggerMode.valueOf(packet.getIntersectionType().name())
    );
    endSplit.personalBest(packet.getPersonalBest());

    this.addon.runState().reset();
    this.addon.runState().splits().clear();
    this.addon.runState().startPosition(toVector(packet.getStartPosition()));
    this.addon.runState().endSplit(endSplit);

    ChatMessage.of("messages.run.loadedFromServer")
        .withColor(NamedTextColor.GREEN)
        .withArgs(Component.text(packet.getName()))
        .send();
  }

  private static DoubleVector3 toVector(DoubleVec3 vec) {
    return new DoubleVector3(vec.getX(), vec.getY(), vec.getZ());
  }
}
