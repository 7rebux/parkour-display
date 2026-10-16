package pw.rebux.parkourdisplay.core.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.labymod.api.Laby;
import net.labymod.api.client.Minecraft;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.entity.player.GameMode;
import net.labymod.api.client.world.block.BlockState;
import net.labymod.api.client.world.item.VanillaItems;
import net.labymod.api.client.world.phys.hit.BlockHitResult;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;
import net.labymod.api.util.math.vector.IntVector3;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorldUtils {

  private static final Minecraft minecraft = Laby.labyAPI().minecraft();

  /// Finds block with collision at or below player
  public static Optional<BlockState> getBlockStandingOn() {
    var player = Objects.requireNonNull(minecraft.getClientPlayer());
    var world = minecraft.clientWorld();
    var position = player.position().toDoubleVector3();

    var inside = Optional.of(world.getBlockState(position))
        .filter(BlockState::hasCollision);
    var below = Optional.of(world.getBlockState(position.sub(0, 1, 0)))
        .filter(BlockState::hasCollision);

    return inside.or(() -> below);
  }

  /// Retrieves the block that the player is currently looking at (within 64 blocks).
  public static Optional<BlockState> getBlockLookingAt() {
    return rayTraceHit(64.0D, 1.0F)
        .map(hit -> minecraft.clientWorld().getBlockState(hit.location()));
  }

  public static Optional<BlockRayTracer.Hit> rayTraceHit(double distance, float partialTicks) {
    var player = Objects.requireNonNull(minecraft.getClientPlayer());
    // Reused across every voxel the ray visits, so the traversal allocates nothing.
    var scratch = new double[6];

    var hit = BlockRayTracer.trace(
        player.eyePosition(),
        new DoubleVector3(player.perspectiveVector(partialTicks)),
        distance,
        (x, y, z) -> {
          var state = minecraft.clientWorld().getBlockState(x, y, z);
          if (state == null) return null;
          if (state.block().isAir()) return null;

          var box = state.bounds();
          if (box == null) return null;

          scratch[0] = box.getMinX();
          scratch[1] = box.getMinY();
          scratch[2] = box.getMinZ();
          scratch[3] = box.getMaxX();
          scratch[4] = box.getMaxY();
          scratch[5] = box.getMaxZ();
          return scratch;
        }
    );

    return Optional.ofNullable(hit);
  }

  public static BlockState getInBlockState(DoubleVector3 position) {
    return minecraft.clientWorld().getBlockState(position);
  }

  public static boolean onClimbable(ClientPlayer player) {
    if (player.gameMode() == GameMode.SPECTATOR) {
      return false;
    }

    if (player.isAbilitiesFlying()) {
      return false;
    }

    var state = getInBlockState(player.position().toDoubleVector3());
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
