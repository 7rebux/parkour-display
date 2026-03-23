package pw.rebux.parkourdisplay.core.command.macro;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

public final class ListMacrosCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public ListMacrosCommand(ParkourDisplayAddon addon) {
    super("listmacros", "lsmacros", "lsmacro");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var files = this.addon.macroFileManager().availableFiles();

    if (files.isEmpty()) {
      ChatMessage.of(this, "empty")
          .withColor(NamedTextColor.RED)
          .send();
      return true;
    }

    files.forEach(file -> {
      ChatMessage.of(this, "entry")
          .withColor(NamedTextColor.GRAY)
          .withArgs(
              Component.text(file.name(), NamedTextColor.YELLOW),
              file.lastModified()
          )
          .send();
    });

    return true;
  }
}
