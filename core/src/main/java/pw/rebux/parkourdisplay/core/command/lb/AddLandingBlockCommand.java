package pw.rebux.parkourdisplay.core.command.lb;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.WorldUtils;

public final class AddLandingBlockCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public AddLandingBlockCommand(ParkourDisplayAddon addon) {
    super("addlb", "setlb");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var blockState = arguments.length > 0
        ? arguments[0].equalsIgnoreCase("target")
            ? WorldUtils.getBlockLookingAt()
            : WorldUtils.getBlockStandingOn()
        : WorldUtils.getBlockStandingOn();

    if (blockState.isEmpty()) {
      this.displayTranslatable("invalidBlock", NamedTextColor.RED);
      return true;
    }

    this.addon.landingBlockManager().register(blockState.get());
    this.displayTranslatable("success", NamedTextColor.GREEN);

    return true;
  }
}
