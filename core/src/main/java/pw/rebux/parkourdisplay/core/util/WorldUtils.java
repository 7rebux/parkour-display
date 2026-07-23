package pw.rebux.parkourdisplay.core.util;

import java.util.Objects;
import java.util.Optional;
import net.labymod.api.Laby;
import net.labymod.api.client.Minecraft;
import net.labymod.api.client.world.block.BlockState;
import net.labymod.api.client.world.phys.hit.BlockHitResult;
import net.labymod.api.util.math.vector.DoubleVector3;

public final class WorldUtils {

  private static final Minecraft minecraft = Laby.labyAPI().minecraft();

  /// A block found by [#getBlockStandingOn()] or [#getBlockLookingAt()], together with the point
  /// that was used to target it. When a block's collision shape is made up of multiple boxes
  /// (e.g. stairs), this point identifies which one the player actually meant.
  public record TargetedBlock(BlockState blockState, DoubleVector3 referencePoint) {

  }

  /// Finds block with collision at or below player
  public static Optional<TargetedBlock> getBlockStandingOn() {
    var world = minecraft.clientWorld();
    var player = Objects.requireNonNull(minecraft.getClientPlayer());
    var position = player.position().toDoubleVector3();

    var inside = Optional.of(world.getBlockState(position))
        .filter(BlockState::hasCollision);
    var below = Optional.of(world.getBlockState(position.sub(0, 1, 0)))
        .filter(BlockState::hasCollision);

    return inside.or(() -> below)
        .map(blockState -> new TargetedBlock(blockState, position));
  }

  /**
   * Retrieves the block that the player is currently looking at.
   * The method uses Minecraft's current hit result and determines the block state
   * present at the position of the targeted block.
   *
   * @return An {@code Optional} containing the targeted block, or an empty {@code Optional} if
   *         there is no block or if the block is air.
   */
  public static Optional<TargetedBlock> getBlockLookingAt() {
    var result = minecraft.getHitResult();
    var blockResult = (BlockHitResult) result;
    var blockState = minecraft.clientWorld().getBlockState(blockResult.getBlockPosition());

    return Optional.of(blockState)
        .filter(bs -> !bs.block().isAir())
        .map(bs -> new TargetedBlock(bs, blockResult.location()));
  }
}
