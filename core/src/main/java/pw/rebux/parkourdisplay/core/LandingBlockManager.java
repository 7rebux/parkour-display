package pw.rebux.parkourdisplay.core;

import java.util.ArrayList;
import lombok.Getter;
import net.labymod.api.client.world.block.BlockState;
import net.labymod.api.util.math.position.Position;
import net.labymod.api.util.math.vector.IntVector3;
import pw.rebux.parkourdisplay.core.state.TickPosition;

@Getter
public class LandingBlockManager {

  private final ArrayList<LandingBlock> landingBlocks = new ArrayList<>();

  public void register(BlockState blockState) {
    var pos = blockState.position();

    this.landingBlocks.add(
        new LandingBlock(
            new IntVector3(pos.getX(), pos.getY(), pos.getZ()),
            blockState.bounds()));
  }

  public void checkOffsets(Position position, TickPosition lastTick, TickPosition secondLastTick) {
    this.landingBlocks.forEach(landingBlock ->
        landingBlock.checkOffsets(position, lastTick, secondLastTick));
  }
}
