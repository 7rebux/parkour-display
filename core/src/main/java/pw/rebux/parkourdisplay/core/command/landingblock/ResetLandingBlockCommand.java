package pw.rebux.parkourdisplay.core.command.landingblock;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import org.spongepowered.include.com.google.common.primitives.Ints;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class ResetLandingBlockCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public ResetLandingBlockCommand(ParkourDisplayAddon addon) {
    super("resetlb");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var landingBlockRegistry = this.addon.landingBlockRegistry();

    if (arguments.length == 0) {
      landingBlockRegistry.landingBlocks().forEach(landingBlock ->
          landingBlock.bestDistance(null));
      this.displayTranslatable("successAll", NamedTextColor.GREEN);
    } else {
      var index = Ints.tryParse(arguments[0]);

      if (index == null || landingBlockRegistry.landingBlocks().size() <= index) {
        this.displayTranslatable("invalidIndex", NamedTextColor.RED);
        return true;
      }

      var landingBlock = landingBlockRegistry.landingBlocks().get(index);

      landingBlock.bestDistance(null);
      this.displayTranslatable("successSingle", NamedTextColor.GREEN);
    }

    return true;
  }
}
