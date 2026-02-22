package pw.rebux.parkourdisplay.core.command.run;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.run.split.Split;
import pw.rebux.parkourdisplay.core.run.split.SplitBoxTriggerMode;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

public final class AddRunSplitCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public AddRunSplitCommand(ParkourDisplayAddon addon) {
    super("addsplit", "setsplit", "as");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var splits = this.addon.runState().splits();
    var offset = arguments.length > 0 ? Double.parseDouble(arguments[0]) / 2 : 1;
    var absoluteBB = new AxisAlignedBoundingBox(
        player.position().getX() - offset,
        player.position().getY(),
        player.position().getZ() - offset,
        player.position().getX() + offset,
        player.position().getY(),
        player.position().getZ() + offset
    );

    splits.add(
        new Split(
            "Split %d".formatted(splits.size() + 1),
            absoluteBB,
            SplitBoxTriggerMode.IntersectXZSameY));

    ChatMessage.of(this, "success").send();

    return true;
  }
}
