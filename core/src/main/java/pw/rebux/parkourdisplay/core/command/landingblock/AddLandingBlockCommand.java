package pw.rebux.parkourdisplay.core.command.landingblock;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.landingblock.LandingBlockMode;
import pw.rebux.parkourdisplay.core.util.ChatMessage;
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
    var mode = LandingBlockMode.Land;

    if (arguments.length > modeArgIndex) {
      for (LandingBlockMode value : LandingBlockMode.values()) {
        if (value.name().equalsIgnoreCase(arguments[modeArgIndex])) {
          mode = value;
          break;
        }
      }
    }

    if (blockState.isEmpty() || !blockState.get().hasCollision()) {
      ChatMessage.of(this, "invalidBlock")
          .withColor(NamedTextColor.RED)
          .send();
      return true;
    }

    this.addon.landingBlockRegistry().register(blockState.get(), mode);
    ChatMessage.of(this, "success")
        .withColor(NamedTextColor.GREEN)
        .withArgs(mode.name())
        .send();

    return true;
  }
}
