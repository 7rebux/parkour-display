package pw.rebux.parkourdisplay.core;

import lombok.RequiredArgsConstructor;
import net.labymod.api.Laby;
import net.labymod.api.client.world.block.BlockState;
import net.labymod.api.util.math.position.Position;
import pw.rebux.parkourdisplay.core.state.TickPosition;

@RequiredArgsConstructor
public class LandingBlock {

  private final BlockState blockState;
  private final LandingBlockOffsets offsets = new LandingBlockOffsets();

  public void checkOffsets(Position position, TickPosition lastTick, TickPosition secondLastTick) {
    // TODO: calculateBounds probably needed
    var collisions = Laby.labyAPI().minecraft().clientWorld().getBlockCollisions(blockState.bounds());

    for (var box : collisions) {
      if (!(position.getY() <= box.getMaxY()) || !(lastTick.y() > box.getMaxY())) {
        continue;
      }

      // Landing mode
      offsets.update(box, lastTick.x(), lastTick.y(), lastTick.z(), secondLastTick.x(), secondLastTick.y(), secondLastTick.z());
    }
  }
}
