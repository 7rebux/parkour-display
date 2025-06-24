package pw.rebux.parkourdisplay.core.command;

import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import org.spongepowered.include.com.google.common.primitives.Ints;
import pw.rebux.parkourdisplay.core.LandingBlockOffsets;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class ResetLandingBlockCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public ResetLandingBlockCommand(ParkourDisplayAddon addon) {
    super("resetlb");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var landingBlockManager = this.addon.landingBlockManager();

    if (arguments.length == 0) {
      landingBlockManager.getLandingBlocks().forEach(landingBlock ->
          landingBlock.offsets(new LandingBlockOffsets()));
      this.displayMessage(
          translatable(
              "parkourdisplay.commands.resetlb.messages.successAll",
              NamedTextColor.GREEN));
    } else {
      var index = Ints.tryParse(arguments[0]);

      if (index == null || landingBlockManager.getLandingBlocks().size() <= index) {
        this.displayMessage(
            translatable(
                "parkourdisplay.commands.resetlb.messages.invalidIndex",
                NamedTextColor.RED));
        return true;
      }

      var landingBlock = landingBlockManager.getLandingBlocks().get(index);

      landingBlock.offsets(new LandingBlockOffsets());
      this.displayMessage(
          translatable(
              "parkourdisplay.commands.resetlb.messages.successSingle",
              NamedTextColor.GREEN));
    }

    return true;
  }
}
