package pw.rebux.parkourdisplay.core.landingblock;

import java.util.ArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.world.block.BlockState;
import net.labymod.api.util.math.vector.IntVector3;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.TickPosition;

@Getter
@RequiredArgsConstructor
public class LandingBlockManager {

  private final ParkourDisplayAddon addon;
  private final ArrayList<LandingBlock> landingBlocks = new ArrayList<>();

  @Getter
  @Setter
  private double lastTotalLandingBlockOffset = 0;

  @Getter
  @Setter
  private double lastLandingBlockOffsetX = 0, lastLandingBlockOffsetZ = 0;

  public void register(BlockState blockState) {
    var pos = blockState.position();

    this.landingBlocks.add(
        new LandingBlock(
            blockState.block(),
            new IntVector3(pos.getX(), pos.getY(), pos.getZ()),
            blockState.bounds()));
  }

  // TODO: Add quick explanation how this works
  // TODO: Refactor all of this logic using AABB calculations
  // TODO: Add hit mode, z neo mode
  public void checkOffsets(ClientPlayer player, TickPosition tickPosition) {
    this.landingBlocks.forEach(landingBlock ->
        landingBlock.checkOffsets(addon, player, tickPosition));
  }
}
