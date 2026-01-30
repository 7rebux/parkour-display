package pw.rebux.parkourdisplay.core.util;

import java.util.Objects;
import java.util.Optional;
import net.labymod.api.Laby;
import net.labymod.api.client.Minecraft;
import net.labymod.api.client.world.block.BlockState;

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
}
