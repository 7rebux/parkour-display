package pw.rebux.parkourdisplay.core.command.macro;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.macro.MacroRotationChange;
import pw.rebux.parkourdisplay.core.macro.MacroTickState;
import pw.rebux.parkourdisplay.core.run.RunTickState;

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

    var inputs = accumulateInputs(this.addon.runState().previousTickStates());
    var name = arguments[0];

    try {
      this.addon.macroFileManager().save(inputs, name);
      this.displayTranslatable("success", NamedTextColor.GREEN);
    } catch (IOException e) {
      this.addon.logger().error("Could not save macro", e);
      this.displayTranslatable("error", NamedTextColor.RED);
    }

    return true;
  }

  private ArrayList<MacroTickState> accumulateInputs(
      List<RunTickState> tickStates
  ) {
    var rotationChange = this.addon.configuration().rotationChange().get();
    var inputs = new ArrayList<MacroTickState>();
    var prevYaw = tickStates.getFirst().position().yaw();
    var prevPitch = tickStates.getFirst().position().pitch();

    for (var entry : tickStates) {
      inputs.add(
          new MacroTickState(
              entry.input().w(),
              entry.input().a(),
              entry.input().s(),
              entry.input().d(),
              entry.input().jump(),
              entry.input().sprint(),
              entry.input().sneak(),
              rotationChange == MacroRotationChange.Absolute
                  ? entry.position().yaw()
                  : entry.position().yaw() - prevYaw,
              rotationChange == MacroRotationChange.Absolute
                  ? entry.position().pitch()
                  : entry.position().pitch() - prevPitch
          )
      );

      prevYaw = entry.position().yaw();
      prevPitch = entry.position().pitch();
    }

    return inputs;
  }
}
