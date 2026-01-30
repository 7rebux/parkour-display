package pw.rebux.parkourdisplay.core.command.lb;

import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.WorldUtils;

public final class AddLandingBlockCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public AddLandingBlockCommand(ParkourDisplayAddon addon) {
    super("addlb", "setlb", "alb", "slb");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var blockState = WorldUtils.getBlockStandingOn();

    if (blockState.isEmpty()) {
      this.displayMessage(
          translatable(
              "parkourdisplay.commands.addlb.messages.invalidBlock",
              NamedTextColor.RED));
      return true;
    }

    this.addon.landingBlockManager().register(blockState.get());
    this.displayMessage(
        translatable(
            "parkourdisplay.commands.addlb.messages.success",
            NamedTextColor.GREEN));

    return true;
  }
}
