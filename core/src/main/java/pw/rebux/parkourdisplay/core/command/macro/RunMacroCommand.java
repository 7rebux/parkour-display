package pw.rebux.parkourdisplay.core.command.macro;

import java.io.FileNotFoundException;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class RunMacroCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public RunMacroCommand(ParkourDisplayAddon addon) {
    super("runmacro", "playmacro", "run", "play");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    try {
      var macro = this.addon.macroFileManager().load((arguments[0]));
      this.addon.macroRunner().execute(macro);
    } catch (FileNotFoundException e) {
      this.displayTranslatable("notFound", NamedTextColor.RED);
    }

    return true;
  }
}
