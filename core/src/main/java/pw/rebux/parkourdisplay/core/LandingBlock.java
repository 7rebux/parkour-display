package pw.rebux.parkourdisplay.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.labymod.api.Laby;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.position.Position;
import net.labymod.api.util.math.vector.IntVector3;
import pw.rebux.parkourdisplay.core.state.TickPosition;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class LandingBlock {

  private final IntVector3 blockPosition;
  private final AxisAlignedBoundingBox boundingBox;
  private final LandingBlockOffsets offsets = new LandingBlockOffsets();

  public void checkOffsets(Position position, TickPosition lastTick, TickPosition secondLastTick) {
    // TODO: calculateBounds probably needed
    var minecraft = Laby.labyAPI().minecraft();
    var blockState = minecraft.clientWorld().getBlockState(blockPosition);

    if (!blockState.hasCollision()) {
      return;
    }

    var collisions = minecraft.clientWorld().getBlockCollisions(blockState.bounds());

    for (var box : collisions) {
      if (!(position.getY() <= box.getMaxY()) || !(lastTick.y() > box.getMaxY())) {
        continue;
      }

      // Landing mode
      offsets.update(box, lastTick.x(), lastTick.y(), lastTick.z(), secondLastTick.x(), secondLastTick.y(), secondLastTick.z());
    }
  }
}
