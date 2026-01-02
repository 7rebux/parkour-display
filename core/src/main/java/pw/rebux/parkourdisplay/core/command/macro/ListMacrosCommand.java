package pw.rebux.parkourdisplay.core.command.macro;

import static net.labymod.api.client.component.Component.space;
import static net.labymod.api.client.component.Component.text;
import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class ListMacrosCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public ListMacrosCommand(ParkourDisplayAddon addon) {
    super("listmacros", "listmacro", "lsmacros", "lsmacro");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var macros = this.addon.macroManager().macros();

    if (macros.isEmpty()) {
      this.displayMessage(
          translatable(
              "parkourdisplay.commands.listmacros.messages.empty",
              NamedTextColor.RED));
      return true;
    }

    macros.forEach((name, macro) ->
        this.displayMessage(
          text("-", NamedTextColor.GRAY)
              .append(space())
              .append(text(name, NamedTextColor.YELLOW))
              .append(space())
              .append(text("%dt".formatted(macro.size()), NamedTextColor.GOLD))));

    return true;
  }
}
