package pw.rebux.parkourdisplay.core.command.landingblock;

import java.util.Arrays;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.Style;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.landingblock.LandingBlockMode;
import pw.rebux.parkourdisplay.core.util.WorldUtils;

public final class AddLandingBlockCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public AddLandingBlockCommand(ParkourDisplayAddon addon) {
    super("addlb", "setlb");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var useTargetBlock = arguments.length > 0 && arguments[0].equalsIgnoreCase("target");
    var blockState = useTargetBlock ? WorldUtils.getBlockLookingAt() : WorldUtils.getBlockStandingOn();
    var modeArgIndex = useTargetBlock ? 1 : 0;
    var mode = arguments.length > modeArgIndex
        ? Arrays.stream(LandingBlockMode.values())
          .filter(v -> v.toString().equalsIgnoreCase(arguments[modeArgIndex]))
          .findFirst()
          .orElse(LandingBlockMode.Land)
        : LandingBlockMode.Land;

    if (blockState.isEmpty() || !blockState.get().hasCollision()) {
      this.displayTranslatable("invalidBlock", Style.EMPTY);
      return true;
    }

    this.addon.landingBlockRegistry().register(blockState.get(), mode);
    this.displayTranslatable("success", NamedTextColor.WHITE, mode.name());

    return true;
  }
}
