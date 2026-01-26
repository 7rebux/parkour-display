package pw.rebux.parkourdisplay.core.command.run;

import static net.labymod.api.client.component.Component.translatable;

import java.io.FileNotFoundException;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class LoadRunSplitsCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public LoadRunSplitsCommand(ParkourDisplayAddon addon) {
    super("loadsplits", "importsplits");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    if (arguments.length == 0) {
      this.displayMessage(
          translatable(
              "parkourdisplay.commands.loadsplits.messages.nameRequired",
              NamedTextColor.RED));
      return true;
    }

    try {
      this.addon.splitManager().loadSplits(arguments[0]);
      this.displayMessage(
          translatable(
              "parkourdisplay.commands.loadsplits.messages.success",
              NamedTextColor.GREEN));
    } catch (FileNotFoundException e) {
      this.displayMessage(
          translatable(
              "parkourdisplay.commands.loadsplits.messages.notFound",
              NamedTextColor.RED));
    }

    return true;
  }
}
