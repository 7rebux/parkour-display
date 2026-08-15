package pw.rebux.parkourdisplay.core.landingblock;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.labymod.api.Laby;
import net.labymod.api.client.world.block.Block;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@Data
@RequiredArgsConstructor
public final class LandingBlockRegistry {

  private final ParkourDisplayAddon addon;
  private final ArrayList<LandingBlock> landingBlocks = new ArrayList<>();

  private double lastTotalLandingBlockOffset = 0;
  private double lastLandingBlockOffsetX = 0, lastLandingBlockOffsetZ = 0;

  public void register(Block block, AxisAlignedBoundingBox aabb, LandingBlockMode mode) {
    var label = Laby.labyAPI().minecraft().getTranslation(
        "block.minecraft.%s".formatted(block.id().getPath()));
    this.landingBlocks.add(new LandingBlock(label, mode, List.of(aabb)));
  }
}
