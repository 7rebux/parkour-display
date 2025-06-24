package pw.rebux.parkourdisplay.core;

import java.util.ArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.world.block.BlockState;
import net.labymod.api.util.math.vector.IntVector3;
import pw.rebux.parkourdisplay.core.state.TickPosition;

@Getter
@RequiredArgsConstructor
public class LandingBlockManager {

  private final ParkourDisplayAddon addon;
  private final ArrayList<LandingBlock> landingBlocks = new ArrayList<>();

  public void register(BlockState blockState) {
    var pos = blockState.position();

    this.landingBlocks.add(
        new LandingBlock(
            blockState.block(),
            new IntVector3(pos.getX(), pos.getY(), pos.getZ()),
            blockState.bounds()));
  }

  public void checkOffsets(
      ClientPlayer player,
      TickPosition lastTick,
      TickPosition secondLastTick
  ) {
    this.landingBlocks.forEach(landingBlock ->
        landingBlock.checkOffsets(addon, player, lastTick, secondLastTick));
  }
}
