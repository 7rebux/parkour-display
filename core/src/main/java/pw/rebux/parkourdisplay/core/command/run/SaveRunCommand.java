package pw.rebux.parkourdisplay.core.command.run;

import java.io.IOException;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class SaveRunCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public SaveRunCommand(ParkourDisplayAddon addon) {
    super("savesplits", "exportsplits", "saverun");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    if (arguments.length == 0) {

      this.displayTranslatable("nameRequired", NamedTextColor.RED);
      return true;
    }

    try {
      this.addon.runFileManager().save(this.addon.runState(), arguments[0]);
      this.displayTranslatable("success", NamedTextColor.GREEN);
    } catch (IOException e) {
      addon.logger().error("Failed to save splits.", e);
      this.displayMessage("Failed to save splits. Check console for more info.");
    }

    return true;
  }
}
