package pw.rebux.parkourdisplay.core.command;

import static net.labymod.api.client.component.Component.translatable;

import java.io.IOException;
import java.util.ArrayList;
import net.labymod.api.client.chat.command.SubCommand;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class SaveMacroCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  protected SaveMacroCommand(ParkourDisplayAddon addon) {
    super("savemacro");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    if (arguments.length == 0) {
      return false;
    }

    var previousTicks = new ArrayList<>(this.addon.playerParkourState().previousTicks());
    var name = arguments[0];

    try {
      this.addon.macroManager().saveMacro(previousTicks, name);
      this.displayMessage(translatable("parkourdisplay.commands.savemacro.messages.success"));
    } catch (IOException e) {
      this.addon.logger().error("Could not save macro with name '%s'".formatted(name), e);
      this.displayMessage(translatable("parkourdisplay.commands.savemacro.messages.error"));
      return false;
    }

    return true;
  }
}
