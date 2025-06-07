package pw.rebux.parkourdisplay.core.command;

import net.labymod.api.client.chat.command.Command;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class BaseCommand extends Command {

  private final ParkourDisplayAddon addon;

  public BaseCommand(ParkourDisplayAddon addon) {
    // Could add cyv and mpk alias here, but maybe others want to use both mods.
    super("parkourdisplay", "pd");

    this.withSubCommand(new SetLandingBlockCommand(addon));

    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    // TODO: Descriptions
    // TODO: Use adventure components with colors
    this.getSubCommands().forEach(subCommand ->
        addon.sendMessage("- %s: Description".formatted(subCommand.getPrefix())));

    return true;
  }
}
