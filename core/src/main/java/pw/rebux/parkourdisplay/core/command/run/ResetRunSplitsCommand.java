package pw.rebux.parkourdisplay.core.command.run;

import net.labymod.api.client.chat.command.SubCommand;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

public final class ResetRunSplitsCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public ResetRunSplitsCommand(ParkourDisplayAddon addon) {
    super("resetsplits", "resetsplit");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var runState = this.addon.runState();

    if (runState.endSplit() != null) {
      runState.endSplit().personalBest(null);
    }

    runState.splits().forEach(split -> split.personalBest(null));
    ChatMessage.of(this, "success").send();

    return true;
  }
}
