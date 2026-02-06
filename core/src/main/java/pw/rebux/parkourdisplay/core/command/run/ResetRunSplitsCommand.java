package pw.rebux.parkourdisplay.core.command.run;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

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

    this.displayTranslatable("success", NamedTextColor.GREEN);

    return true;
  }
}
