package pw.rebux.parkourdisplay.core.command;

import java.util.Optional;
import net.labymod.api.client.chat.command.SubCommand;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class RunMacroCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  protected RunMacroCommand(ParkourDisplayAddon addon) {
    super("runmacro", "execmacro", "playmacro");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    if (arguments.length == 0) {
      return false;
    }

    var name = arguments[0];
    var macroOptional = Optional.ofNullable(this.addon.macroManager().macros().get(name));

    if (macroOptional.isEmpty()) {
      // TODO: Not found
      return false;
    }

    this.addon.macroManager().runMacro(macroOptional.get());

    return true;
  }
}
