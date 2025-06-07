package pw.rebux.parkourdisplay.core;

import net.labymod.api.client.world.block.BlockState;
import net.labymod.api.util.math.position.Position;
import pw.rebux.parkourdisplay.core.state.TickPosition;
import java.util.ArrayList;

public class LandingBlockManager {

  private final ArrayList<LandingBlock> landingBlocks = new ArrayList<>();

  public void register(BlockState blockState) {
    this.landingBlocks.add(new LandingBlock(blockState));
  }

  public void checkOffsets(Position position, TickPosition lastTick, TickPosition secondLastTick) {
    this.landingBlocks.forEach(landingBlock ->
        landingBlock.checkOffsets(position, lastTick, secondLastTick));
  }
}
