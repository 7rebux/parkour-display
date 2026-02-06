package pw.rebux.parkourdisplay.core.command.run;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class ClearRunCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public ClearRunCommand(ParkourDisplayAddon addon) {
    super("clearrun", "resetrun", "cr");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var runState = this.addon.runState();

    runState.reset();
    runState.splits().clear();
    runState.startPosition(null);
    runState.endSplit(null);

    this.displayTranslatable("success", NamedTextColor.GREEN);

    return true;
  }
}
