package pw.rebux.parkourdisplay.core;

import java.util.ArrayList;
import lombok.Getter;
import net.labymod.api.client.world.block.BlockState;
import net.labymod.api.util.math.position.Position;
import pw.rebux.parkourdisplay.core.state.TickPosition;

@Getter
public class LandingBlockManager {

  private final ArrayList<LandingBlock> landingBlocks = new ArrayList<>();

  public void register(BlockState blockState) {
    this.landingBlocks.add(new LandingBlock(blockState.position(), blockState.bounds()));
  }

  public void checkOffsets(Position position, TickPosition lastTick, TickPosition secondLastTick) {
    this.landingBlocks.forEach(landingBlock ->
        landingBlock.checkOffsets(position, lastTick, secondLastTick));
  }
}
