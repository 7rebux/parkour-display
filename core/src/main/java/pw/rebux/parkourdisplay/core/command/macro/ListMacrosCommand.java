package pw.rebux.parkourdisplay.core.command.macro;

import static net.labymod.api.client.component.Component.space;
import static net.labymod.api.client.component.Component.text;

import java.text.SimpleDateFormat;
import java.util.Date;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class ListMacrosCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  private static final SimpleDateFormat DATE_FORMAT =
      new SimpleDateFormat("yyyy-MM-dd HH:mm");

  public ListMacrosCommand(ParkourDisplayAddon addon) {
    super("listmacros", "lsmacros", "lsmacro");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var files = this.addon.macroFileManager().availableFiles();

    if (files.isEmpty()) {
      this.displayTranslatable("empty", NamedTextColor.RED);
      return true;
    }

    files.forEach(file -> {
      var formattedDate = DATE_FORMAT.format(new Date(file.lastModified()));
      this.displayMessage(
          text("-", NamedTextColor.GRAY)
              .append(space())
              .append(text(file.name(), NamedTextColor.YELLOW))
              .append(space())
              .append(text("[%s]".formatted(formattedDate), NamedTextColor.GRAY)));
    });

    return true;
  }
}
