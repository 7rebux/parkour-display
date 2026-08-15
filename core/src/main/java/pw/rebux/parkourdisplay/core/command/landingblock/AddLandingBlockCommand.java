package pw.rebux.parkourdisplay.core.command.landingblock;

import java.util.Comparator;
import java.util.Objects;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.world.block.BlockState;
import net.labymod.api.util.Pair;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import org.jspecify.annotations.Nullable;
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

    var result = useTargetBlock ? findTargetBlock() : findBlockStandingOn();

    if (result == null) {
      ChatMessage.of(this, "invalidBlock")
          .withColor(NamedTextColor.RED)
          .send();
      return true;
    }

    this.addon.landingBlockRegistry().register(result.getFirst().block(), result.getSecond(), mode);

    ChatMessage.of(this, "success")
        .withColor(NamedTextColor.GREEN)
        .withArgs(mode.name())
        .send();

    return true;
  }

  private @Nullable Pair<BlockState, AxisAlignedBoundingBox> findTargetBlock() {
    var world = this.addon.labyAPI().minecraft().clientWorld();
    var hitResultOptional = WorldUtils.rayTraceHit(64.0D, 1.0F);

    if (hitResultOptional.isEmpty()) return null;

    var hitResult = hitResultOptional.get();
    var blockState = world.getBlockState(hitResult.location());
    var absoluteBounds = Objects.requireNonNull(blockState.bounds()).move(hitResult.location());

    var aabb = world.getBlockCollisions(absoluteBounds).stream()
        .min(Comparator.comparingDouble(box ->
            BoundingBoxUtils.distanceToPoint(box, hitResult.hit())))
        .orElse(null);

    if (aabb == null) return null;

    return Pair.of(blockState, aabb);
  }

  private @Nullable Pair<BlockState, AxisAlignedBoundingBox> findBlockStandingOn() {
    var world = this.addon.labyAPI().minecraft().clientWorld();
    var player = Objects.requireNonNull(this.addon.labyAPI().minecraft().getClientPlayer());
    var blockStateOptional = WorldUtils.getBlockStandingOn();

    if (blockStateOptional.isEmpty()) return null;

    var blockState = blockStateOptional.get();
    var absoluteBounds = Objects.requireNonNull(blockState.bounds()).move(blockState.position());

    var aabb = world.getBlockCollisions(absoluteBounds).stream()
        .min(Comparator.comparingDouble(b ->
            Math.abs(b.getMaxY() - player.position().getY())))
        .orElse(null);

    if (aabb == null) return null;

    return Pair.of(blockState, aabb);
  }
}
