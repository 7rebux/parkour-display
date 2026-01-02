package pw.rebux.parkourdisplay.core.command.run;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import net.labymod.api.client.chat.command.SubCommand;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class SaveRunSplitsCommand extends SubCommand {

  private static final File RUN_SPLITS_DIR = new File(ParkourDisplayAddon.DATA_DIR, "splits");

  private final ParkourDisplayAddon addon;

  public SaveRunSplitsCommand(ParkourDisplayAddon addon) {
    super("savesplits", "exportsplits");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    if (arguments.length == 0) {

      return false;
    }

    var name = arguments[0];

    try {
      var writer = new BufferedWriter(new FileWriter(new File(RUN_SPLITS_DIR, name + ".json")));

      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return true;
  }
}
