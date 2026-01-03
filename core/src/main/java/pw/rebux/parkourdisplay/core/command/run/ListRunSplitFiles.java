package pw.rebux.parkourdisplay.core.command.run;

import static net.labymod.api.client.component.Component.space;
import static net.labymod.api.client.component.Component.text;
import static net.labymod.api.client.component.Component.translatable;

import java.text.SimpleDateFormat;
import java.util.Date;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class ListRunSplitFiles extends SubCommand {

  private final ParkourDisplayAddon addon;

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  public ListRunSplitFiles(ParkourDisplayAddon addon) {
    super("listsplitfiles");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
      var files = addon.splitsManager().listAvailableFiles();

      if (files.isEmpty()) {
        this.displayMessage(
            translatable(
                "parkourdisplay.commands.listsplitfiles.messages.empty",
                NamedTextColor.RED));
        return true;
      }

      files.forEach(file -> {
        var formattedDate = DATE_FORMAT.format(new Date(file.lastModified()));
        this.displayMessage(
            text("-", NamedTextColor.GRAY)
                .append(space())
                .append(text(file.fileName(), NamedTextColor.YELLOW))
                .append(space())
                .append(text("(%s)".formatted(file.type()), NamedTextColor.LIGHT_PURPLE))
                .append(space())
                .append(text("[%s]".formatted(formattedDate), NamedTextColor.GRAY)));
      });

      return true;
    }
}
