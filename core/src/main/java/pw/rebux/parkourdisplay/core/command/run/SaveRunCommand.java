package pw.rebux.parkourdisplay.core.command.run;

import java.io.IOException;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

public final class SaveRunCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public SaveRunCommand(ParkourDisplayAddon addon) {
    super("savesplits", "exportsplits", "saverun");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    if (arguments.length == 0) {
      ChatMessage.of(this, "nameRequired")
          .withColor(NamedTextColor.RED)
          .send();
      return true;
    }

    try {
      this.addon.runFileManager().save(this.addon.runState(), arguments[0]);
      ChatMessage.of(this, "success")
          .withColor(NamedTextColor.GREEN)
          .send();
    } catch (IOException e) {
      addon.logger().error("Failed to save splits.", e);
      ChatMessage.of(this, "error")
          .withColor(NamedTextColor.RED)
          .send();
    }

    return true;
  }
}
