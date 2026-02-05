package pw.rebux.parkourdisplay.core.landingblock;

import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.world.block.Block;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.IntVector3;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.TickPosition;

@Getter
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
      TickPosition tickPosition
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
      if (!(position.getY() <= box.getMaxY() && tickPosition.y() > box.getMaxY())) {
        continue;
      }

      // Check if the landing block is close enough
      if (box.getCenter().distanceSquared(position.toDoubleVector3()) > 2) {
        continue;
      }

      offsets.compute(player, box, tickPosition.x(), tickPosition.z());
    }

    offsets.update(addon);
  }
}
