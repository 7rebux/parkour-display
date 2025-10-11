package pw.rebux.parkourdisplay.core.command;

import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.chat.command.SubCommand;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class ReloadMacrosCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  protected ReloadMacrosCommand(ParkourDisplayAddon addon) {
    super("reloadmacros", "rlmacros");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    this.addon.macroManager().loadMacros();
    this.displayMessage(translatable("parkourdisplay.commands.reloadmacros.messages.success"));

    return true;
  }
}
