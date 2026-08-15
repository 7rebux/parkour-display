package pw.rebux.parkourdisplay.core.command.landingblock;

import java.util.Comparator;
import java.util.Objects;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.landingblock.LandingBlockMode;
import pw.rebux.parkourdisplay.core.util.BoundingBoxUtils;
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
    var world = this.addon.labyAPI().minecraft().clientWorld();
    var useTargetBlock = arguments.length > 0 && arguments[0].equalsIgnoreCase("target");
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

    var hitResultOptional = WorldUtils.rayTraceHit(64.0D, 1.0F);

    if (hitResultOptional.isEmpty()) {
      ChatMessage.of(this, "invalidBlock")
          .withColor(NamedTextColor.RED)
          .send();
      return true;
    }

    var hitResult = hitResultOptional.get();
    var blockState = world.getBlockState(hitResult.location());
    var absoluteBounds = Objects.requireNonNull(blockState.bounds()).move(hitResult.location());

    var aabb = world.getBlockCollisions(absoluteBounds).stream()
        .min(Comparator.comparingDouble(box ->
            BoundingBoxUtils.distanceToPoint(box, hitResult.hit())))
        .orElse(null);

    if (aabb == null) {
      ChatMessage.of(this, "invalidBlock")
          .withColor(NamedTextColor.RED)
          .send();
      return true;
    }

    this.addon.landingBlockRegistry().register(blockState.block(), aabb, mode);

    ChatMessage.of(this, "success")
        .withColor(NamedTextColor.GREEN)
        .withArgs(mode.name())
        .send();

    return true;
  }
}
