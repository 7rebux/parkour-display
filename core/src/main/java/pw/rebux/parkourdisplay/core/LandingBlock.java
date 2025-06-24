package pw.rebux.parkourdisplay.core;

import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.world.block.Block;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.IntVector3;
import pw.rebux.parkourdisplay.core.state.TickPosition;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class LandingBlock {

  private final Block block;
  private final IntVector3 blockPosition;
  private final AxisAlignedBoundingBox boundingBox;

  @Setter
  private LandingBlockOffsets offsets = new LandingBlockOffsets();

  public void checkOffsets(
      ParkourDisplayAddon addon,
      ClientPlayer player,
      TickPosition lastTick,
      TickPosition secondLastTick
  ) {
    var position = player.position();
    var minecraft = Laby.labyAPI().minecraft();
    var blockState = minecraft.clientWorld().getBlockState(blockPosition);

    if (!blockState.hasCollision()) {
      return;
    }

    var collisions = minecraft.clientWorld().getBlockCollisions(
        Objects.requireNonNull(blockState.bounds()).move(blockPosition));

    for (var box : collisions) {
      // Check for landing tick
      if (!(position.getY() <= box.getMaxY() && lastTick.y() > box.getMaxY())) {
        continue;
      }

      // Check if the landing block is close enough
      if (box.getCenter().distanceSquared(position.toDoubleVector3()) > 2) {
        continue;
      }

      offsets.compute(
          player,
          box,
          lastTick.x(),
          lastTick.y(),
          lastTick.z(),
          secondLastTick.x(),
          secondLastTick.y(),
          secondLastTick.z());
    }

    offsets.update(addon);
  }
}
