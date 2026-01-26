package pw.rebux.parkourdisplay.core.command.run;

import static net.labymod.api.client.component.Component.translatable;

import java.io.IOException;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class SaveRunSplitsCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public SaveRunSplitsCommand(ParkourDisplayAddon addon) {
    super("savesplits", "exportsplits");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    if (arguments.length == 0) {
      this.displayMessage(
          translatable(
            "parkourdisplay.commands.savesplits.messages.nameRequired",
            NamedTextColor.RED));
      return true;
    }

    try {
      this.addon.splitManager().saveCurrentSplits(arguments[0]);
      this.displayMessage(
          translatable(
              "parkourdisplay.commands.savesplits.messages.success",
              NamedTextColor.GREEN));
    } catch (IOException e) {
      addon.logger().error("Failed to save splits.", e);
      this.displayMessage("Failed to save splits. Check console for more info.");
    }

    return true;
  }
}
