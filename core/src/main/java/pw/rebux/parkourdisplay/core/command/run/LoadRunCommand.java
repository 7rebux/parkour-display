package pw.rebux.parkourdisplay.core.command.run;

import java.io.FileNotFoundException;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class LoadRunCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public LoadRunCommand(ParkourDisplayAddon addon) {
    super("loadsplits", "importsplits", "loadrun");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var runState = this.addon.runState();

    if (arguments.length == 0) {
      this.displayTranslatable("nameRequired", NamedTextColor.RED);
      return true;
    }

    var name = arguments[0];
    var isZM = arguments.length > 1 && arguments[1].equalsIgnoreCase("zm");

    try {
      var data = isZM
          ? this.addon.runFileManager().loadFromZM(name)
          : this.addon.runFileManager().load(name);

      runState.reset();
      runState.startPosition(data.start());
      runState.endSplit(data.end());
      runState.splits(data.splits());

      this.displayTranslatable("success", NamedTextColor.GREEN);
    } catch (FileNotFoundException e) {
      this.displayTranslatable("notFound", NamedTextColor.RED);
    }

    return true;
  }
}
