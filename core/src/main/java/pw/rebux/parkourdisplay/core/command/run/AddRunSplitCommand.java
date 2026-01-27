package pw.rebux.parkourdisplay.core.command.run;

import static net.labymod.api.client.component.Component.translatable;

import java.util.Objects;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.run.PositionOffset;
import pw.rebux.parkourdisplay.core.run.split.RunSplit;

public final class AddRunSplitCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public AddRunSplitCommand(ParkourDisplayAddon addon) {
    super("addsplit", "setsplit", "as");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var player = Objects.requireNonNull(this.addon.labyAPI().minecraft().getClientPlayer());
    var runSplits = this.addon.runState().runSplits();
    var offsetX = arguments.length > 0 ? Double.parseDouble(arguments[0]) : 3;
    var offsetZ = arguments.length > 1 ? Double.parseDouble(arguments[1]) : 3;

    runSplits.add(
        new RunSplit(
            "Split %d".formatted(runSplits.size() + 1),
            PositionOffset.builder()
                .posX(player.position().getX())
                .posY(player.position().getY())
                .posZ(player.position().getZ())
                .offsetX(offsetX)
                .offsetZ(offsetZ)
                .build()));

    this.displayMessage(
        translatable(
          "parkourdisplay.commands.addsplit.messages.success",
          NamedTextColor.GREEN));

    return true;
  }
}
