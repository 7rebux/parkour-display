package pw.rebux.parkourdisplay.core.command.macro;

import java.io.IOException;
import java.util.ArrayList;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.macro.MacroRotationChange;
import pw.rebux.parkourdisplay.core.macro.MacroTickState;

public final class SaveMacroCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public SaveMacroCommand(ParkourDisplayAddon addon) {
    super("savemacro");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    if (arguments.length == 0) {
      this.displayTranslatable("nameRequired", NamedTextColor.RED);
      return true;
    }

    var isAbsolute =
        this.addon.configuration().rotationChange().get() == MacroRotationChange.Absolute;

    // TODO: Handle absolute vs relative mode
    var macroTickStates = new ArrayList<>(this.addon.runState().previousTickStates()).stream()
        .map(state -> new MacroTickState(
            state.input().w(),
            state.input().a(),
            state.input().s(),
            state.input().d(),
            state.input().jump(),
            state.input().sprint(),
            state.input().sneak(),
            state.position().yaw(),
            state.position().pitch()
        ))
        .toList();

    try {
      this.addon.macroManager().saveMacro(macroTickStates, arguments[0]);
      this.displayTranslatable("success", NamedTextColor.GREEN);
    } catch (IOException e) {
      this.addon.logger().error("Could not save macro", e);
      this.displayTranslatable("error", NamedTextColor.RED);
    }

    return true;
  }
}
