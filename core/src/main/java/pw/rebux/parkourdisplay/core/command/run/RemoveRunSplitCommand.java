package pw.rebux.parkourdisplay.core.command.run;

import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class RemoveRunSplitCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public RemoveRunSplitCommand(ParkourDisplayAddon addon) {
    super("removesplit", "deletesplit");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    this.addon.runState().splits().removeLast();

    this.displayMessage(
        translatable(
            "parkourdisplay.commands.removesplit.messages.success",
            NamedTextColor.GREEN));

    return true;
  }
}
