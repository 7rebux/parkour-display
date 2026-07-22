package pw.rebux.parkourdisplay.core.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.labymod.api.Laby;
import net.labymod.api.client.Minecraft;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.entity.player.GameMode;
import net.labymod.api.client.world.block.BlockState;
import net.labymod.api.client.world.item.VanillaItems;
import net.labymod.api.client.world.phys.hit.BlockHitResult;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.IntVector3;

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

  /**
   * Retrieves the block that the player is currently looking at.
   * The method uses Minecraft's current hit result and determines the block state
   * present at the position of the targeted block.
   *
   * @return An {@code Optional} containing the {@code BlockState} of the block the player is
   *         looking at, or an empty {@code Optional} if there is no block or if the block is air.
   */
  public static Optional<BlockState> getBlockLookingAt() {
    var result = minecraft.getHitResult();
    var blockResult = (BlockHitResult) result;
    var blockState = minecraft.clientWorld().getBlockState(blockResult.getBlockPosition());

    return Optional.of(blockState).filter(bs -> !bs.block().isAir());
  }

  public static BlockState getInBlockState(ClientPlayer player) {
    return minecraft.clientWorld().getBlockState(player.position().toDoubleVector3());
  }

  public static boolean onClimbable(ClientPlayer player) {
    if (player.gameMode() == GameMode.SPECTATOR) {
      return false;
    }

    if (player.isAbilitiesFlying()) {
      return false;
    }

    var state = getInBlockState(player);
    return isClimbable(state);
  }

  public static boolean isClimbable(BlockState blockState) {
    return blockState.block().is(VanillaItems.LADDER)
        || blockState.block().is(VanillaItems.VINE);
  }

  public static Set<AxisAlignedBoundingBox> findConnectedLadders(IntVector3 startPos) {
    var world = minecraft.clientWorld();
    var result = new HashSet<AxisAlignedBoundingBox>();
    var position = new IntVector3(startPos);

    while (true) {
      var blockState = world.getBlockState(position);

      if (!isClimbable(blockState)) {
        break;
      }

      result.add(blockState.bounds().move(position));
      position.add(0, 1, 0);
    }

    position = new IntVector3(startPos);

    while (true) {
      var blockState = world.getBlockState(position);

      if (!isClimbable(blockState)) {
        break;
      }

      result.add(blockState.bounds().move(position));
      position.sub(0, 1, 0);
    }

    return result;
  }
}
