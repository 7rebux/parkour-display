package pw.rebux.parkourdisplay.core.command.run;

import static net.labymod.api.client.component.Component.translatable;

import java.util.Objects;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.splits.RunSplit;
import pw.rebux.parkourdisplay.core.state.PositionOffset;

public final class SetRunEndCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public SetRunEndCommand(ParkourDisplayAddon addon) {
    super("setend", "se");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var player = Objects.requireNonNull(this.addon.labyAPI().minecraft().getClientPlayer());
    var offsetX = arguments.length > 0 ? Double.parseDouble(arguments[0]) : 3.0;
    var offsetZ = arguments.length > 1 ? Double.parseDouble(arguments[1]) : 3.0;

    this.addon.playerParkourState().runEndSplit(
        new RunSplit(
            PositionOffset.builder()
              .posX(player.position().getX())
              .posY(player.position().getY())
              .posZ(player.position().getZ())
              .offsetX(offsetX)
              .offsetZ(offsetZ)
              .build()));

    this.addon.displayMessage(
        translatable(
            "parkourdisplay.commands.setend.messages.success",
            NamedTextColor.GREEN));

    return true;
  }
}
