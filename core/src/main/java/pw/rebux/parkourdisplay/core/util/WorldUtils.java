package pw.rebux.parkourdisplay.core.util;

import java.util.Objects;
import java.util.Optional;
import net.labymod.api.Laby;
import net.labymod.api.client.Minecraft;
import net.labymod.api.client.world.block.BlockState;
import net.labymod.api.util.math.MathHelper;

public final class WorldUtils {

  private static final Minecraft minecraft = Laby.labyAPI().minecraft();

  /// Finds block with collision at or below player
  public static Optional<BlockState> getBlockStandingOn() {
    var world = minecraft.clientWorld();
    var player = Objects.requireNonNull(minecraft.getClientPlayer());
    var position = player.position().toDoubleVector3();

    var inside = Optional.of(world.getBlockState(position))
        .filter(BlockState::hasCollision);
    var below = Optional.of(world.getBlockState(position.sub(0, 1 , 0)))
        .filter(BlockState::hasCollision);

    return inside.or(() -> below);
  }

  // TODO: Check this cameraentity getlookingat function for checking how we can check if we raytrace a hitbox from a run tick
  public static Optional<BlockState> getBlockLookingAt() {
    var player = minecraft.getClientPlayer();

    if (player == null) {
      return Optional.empty();
    }

    var targetVector = player.getTargetBlock(10, 1, minecraft.getPartialTicks());
    var blockState = minecraft.clientWorld().getBlockState(
        MathHelper.floor(targetVector.getX()),
        MathHelper.floor(targetVector.getY()),
        MathHelper.floor(targetVector.getZ())
    );

    return Optional.ofNullable(blockState);
  }
}
