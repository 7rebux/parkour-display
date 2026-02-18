package pw.rebux.parkourdisplay.core.landingblock;

import static net.labymod.api.client.component.Component.translatable;

import java.util.ArrayList;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.TranslatableComponent;
import net.labymod.api.client.world.block.Block;
import net.labymod.api.client.world.block.BlockState;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@Data
@RequiredArgsConstructor
public class LandingBlockRegistry {

  private final ParkourDisplayAddon addon;
  private final ArrayList<LandingBlock> landingBlocks = new ArrayList<>();

  private double lastTotalLandingBlockOffset = 0;
  private double lastLandingBlockOffsetX = 0, lastLandingBlockOffsetZ = 0;

  public void register(BlockState blockState, LandingBlockMode mode) {
    var world = this.addon.labyAPI().minecraft().clientWorld();
    var collisions = world.getBlockCollisions(blockState.bounds().move(blockState.position()));

    this.landingBlocks.add(
        new LandingBlock(blockDisplayName(blockState.block()), mode, collisions));
  }

  // TODO: This is not working
  private TranslatableComponent blockDisplayName(Block block) {
    return translatable("block.minecraft.%s".formatted(block.id().getPath()));
  }
}
