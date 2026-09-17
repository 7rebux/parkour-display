package pw.rebux.parkourdisplay.core.ladderbox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.labymod.api.client.world.block.BlockState;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.WorldUtils;

@Data
@RequiredArgsConstructor
public final class LadderBoxRegistry {

  private final ParkourDisplayAddon addon;

  private final List<LadderBox> ladderBoxes = new ArrayList<>();

  private double lastTotalLadderBoxOffset = 0;
  private double lastLadderBoxOffsetX = 0, lastLadderBoxOffsetZ = 0;

  public void register(BlockState blockState) {
    var boundingBoxes = WorldUtils.findConnectedLadders(blockState.position());
    var mainLadderBB = blockState.bounds().move(blockState.position());

    var min = boundingBoxes.stream()
        .min(Comparator.comparingDouble(AxisAlignedBoundingBox::getMinY))
        .orElseThrow();
    var max = boundingBoxes.stream()
        .max(Comparator.comparingDouble(AxisAlignedBoundingBox::getMaxY))
        .orElseThrow();

    var boundingBox = new AxisAlignedBoundingBox(
        mainLadderBB.getMinX(),
        min.getMinY(),
        mainLadderBB.getMinZ(),
        mainLadderBB.getMaxX(),
        max.getMaxY(),
        mainLadderBB.getMaxZ()
    );

    this.ladderBoxes.add(new LadderBox(boundingBox, intersectionBox(boundingBox)));
  }

  /// Expands the thin ladder bounding box to the climbable intersection region: the block column
  /// the ladder sits in, inset by 0.3 on X and Z. Y is left untouched.
  private static AxisAlignedBoundingBox intersectionBox(AxisAlignedBoundingBox box) {
    var blockX = Math.floor(box.getMinX());
    var blockZ = Math.floor(box.getMinZ());

    return new AxisAlignedBoundingBox(
        blockX + 0.3,
        box.getMinY(),
        blockZ + 0.3,
        blockX + 0.7,
        box.getMaxY(),
        blockZ + 0.7
    );
  }
}
