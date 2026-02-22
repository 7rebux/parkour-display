package pw.rebux.parkourdisplay.core.command.run;

import net.labymod.api.client.chat.command.SubCommand;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

public final class RemoveRunSplitCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public RemoveRunSplitCommand(ParkourDisplayAddon addon) {
    super("removesplit", "deletesplit", "rmsplit");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    this.addon.runState().splits().removeLast();
    ChatMessage.of(this, "success").send();
    return true;
  }
}
