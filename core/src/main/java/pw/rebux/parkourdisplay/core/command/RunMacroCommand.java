package pw.rebux.parkourdisplay.core.command;

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
    return false;
  }
}
