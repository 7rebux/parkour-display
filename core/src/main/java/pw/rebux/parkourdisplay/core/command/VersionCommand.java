package pw.rebux.parkourdisplay.core.command;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

public final class VersionCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public VersionCommand(ParkourDisplayAddon addon) {
    super("version", "v");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    ChatMessage.of(this, "success")
        .withArgs(this.addon.addonInfo().getVersion())
        .withColor(NamedTextColor.GREEN)
        .send();

    return true;
  }
}
