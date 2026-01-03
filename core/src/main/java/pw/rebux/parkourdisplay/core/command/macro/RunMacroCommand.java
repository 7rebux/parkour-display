package pw.rebux.parkourdisplay.core.command.macro;

import static net.labymod.api.client.component.Component.translatable;

import java.util.Optional;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class RunMacroCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public RunMacroCommand(ParkourDisplayAddon addon) {
    super("runmacro", "execmacro", "playmacro", "play");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var macroOptional = arguments.length == 1
        ? Optional.ofNullable(this.addon.macroManager().macros().get(arguments[0]))
        : Optional.of(this.addon.playerParkourState().runTickInputs().stream().toList());

    if (macroOptional.isEmpty()) {
      this.displayMessage(
          translatable(
              "parkourdisplay.commands.runmacro.messages.notFound",
              NamedTextColor.RED));
      return true;
    }

    this.addon.macroManager().runMacro(macroOptional.get());

    return true;
  }
}
