package pw.rebux.parkourdisplay.core.command;

import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import org.spongepowered.include.com.google.common.primitives.Ints;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class RemoveLandingBlockCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public RemoveLandingBlockCommand(ParkourDisplayAddon addon) {
    super("removelb", "rmlb", "clearlb");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var landingBlockManager = this.addon.landingBlockManager();

    if (arguments.length == 0) {
      landingBlockManager.getLandingBlocks().clear();
      this.displayMessage(
          translatable(
              "parkourdisplay.commands.removelb.messages.successAll",
              NamedTextColor.GREEN));
    } else {
      var index = Ints.tryParse(arguments[0]);

      if (index == null || landingBlockManager.getLandingBlocks().size() <= index) {
        this.displayMessage(
            translatable(
                "parkourdisplay.commands.removelb.messages.invalidIndex",
                NamedTextColor.RED));
        return true;
      }

      landingBlockManager.getLandingBlocks().remove(index.intValue());
      this.displayMessage(
          translatable(
              "parkourdisplay.commands.removelb.messages.successSingle",
              NamedTextColor.GREEN));
    }

    return true;
  }
}
