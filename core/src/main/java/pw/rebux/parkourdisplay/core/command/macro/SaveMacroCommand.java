package pw.rebux.parkourdisplay.core.command.macro;

import static net.labymod.api.client.component.Component.translatable;

import java.io.IOException;
import java.util.ArrayList;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class SaveMacroCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public SaveMacroCommand(ParkourDisplayAddon addon) {
    super("savemacro");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    if (arguments.length == 0) {
      this.displayMessage(
          translatable(
              "parkourdisplay.commands.savemacro.messages.nameRequired",
              NamedTextColor.RED));
      return true;
    }

    var tickInputs = new ArrayList<>(this.addon.runState().runTickInputs());

    try {
      this.addon.macroManager().saveMacro(tickInputs, arguments[0]);
      this.displayMessage(
          translatable(
              "parkourdisplay.commands.savemacro.messages.success",
              NamedTextColor.GREEN));
    } catch (IOException e) {
      this.addon.logger().error("Could not save macro", e);
      this.displayMessage(
          translatable(
            "parkourdisplay.commands.savemacro.messages.error",
              NamedTextColor.RED));
    }

    return true;
  }
}
