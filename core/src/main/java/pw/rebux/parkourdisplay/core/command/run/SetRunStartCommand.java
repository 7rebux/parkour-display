package pw.rebux.parkourdisplay.core.command.run;

import static net.labymod.api.client.component.Component.translatable;

import java.util.Objects;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.run.PositionOffset;

public final class SetRunStartCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public SetRunStartCommand(ParkourDisplayAddon addon) {
    super("setstart", "ss");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var player = Objects.requireNonNull(this.addon.labyAPI().minecraft().getClientPlayer());
    var offsetX = arguments.length > 0 ? Double.parseDouble(arguments[0]) : 0.1;
    var offsetZ = arguments.length > 1 ? Double.parseDouble(arguments[1]) : 0.1;

    this.addon.runState().runStartPosition(
        PositionOffset.builder()
            .posX(player.position().getX())
            .posY(player.position().getY())
            .posZ(player.position().getZ())
            .offsetX(offsetX)
            .offsetZ(offsetZ)
            .build());

    this.addon.displayMessage(
        translatable(
            "parkourdisplay.commands.setstart.messages.success",
            NamedTextColor.GREEN));

    return true;
  }
}
