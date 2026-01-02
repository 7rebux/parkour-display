package pw.rebux.parkourdisplay.core.command.run;

import java.io.File;
import net.labymod.api.client.chat.command.SubCommand;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class LoadRunSplitsCommand extends SubCommand {

  private static final File ZORTMOD_DATA_DIR = new File("zmdata");

  private final ParkourDisplayAddon addon;

  public LoadRunSplitsCommand(ParkourDisplayAddon addon) {
    super("loadsplits", "importsplits");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    if (arguments.length == 0) {

      return false;
    }

    var name = arguments[0];

    return true;
  }
}
