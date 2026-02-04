package pw.rebux.parkourdisplay.core.command.run;

import java.io.FileNotFoundException;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class LoadRunSplitsCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public LoadRunSplitsCommand(ParkourDisplayAddon addon) {
    super("loadsplits", "importsplits");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    if (arguments.length == 0) {
      this.displayTranslatable("nameRequired", NamedTextColor.RED);
      return true;
    }

    var isZM = arguments.length > 1 && arguments[1].equalsIgnoreCase("zm");

    try {
      if (isZM) {
        this.addon.runManager().loadFromZM(arguments[0]);
      } else {
        this.addon.runManager().load(arguments[0]);
      }

      this.displayTranslatable("success", NamedTextColor.GREEN);
    } catch (FileNotFoundException e) {
      this.displayTranslatable("notFound", NamedTextColor.RED);
    }

    return true;
  }
}
