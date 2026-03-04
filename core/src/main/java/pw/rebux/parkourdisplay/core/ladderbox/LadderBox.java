package pw.rebux.parkourdisplay.core.ladderbox;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.labymod.api.util.math.AxisAlignedBoundingBox;

@Data
@RequiredArgsConstructor
public class LadderBox {

  private final AxisAlignedBoundingBox boundingBox;

  @Getter(lazy = true)
  private final AxisAlignedBoundingBox intersectionBox = expand(this.boundingBox);

  /**
   * Expands the ladder bounding box outward into the climbable intersection region.
   * The box extends 0.7 outward from the climbable face in the correct direction
   * for both positive and negative axes.
   */
  private AxisAlignedBoundingBox expand(AxisAlignedBoundingBox box) {
    var narrowIsX = box.getXWidth() <= box.getZWidth();
    var narrowMin = narrowIsX ? box.getMinX() : box.getMinZ();
    var narrowMax = narrowIsX ? box.getMaxX() : box.getMaxZ();
    var wideMin = narrowIsX ? box.getMinZ() : box.getMinX();
    var wideMax = narrowIsX ? box.getMaxZ() : box.getMaxX();

    // If the narrow range is in the lower part of the block, face is at max; else at min
    var faceAtMax = (narrowMin - Math.floor(narrowMin)) < 0.5;
    var newNarrowMin = faceAtMax ? narrowMax : narrowMax - 0.7;
    var newNarrowMax = faceAtMax ? narrowMin + 0.7 : narrowMin;
    var newWideMin = wideMin + 0.3;
    var newWideMax = wideMax - 0.3;

    if (narrowIsX) {
      return box.minX(newNarrowMin).maxX(newNarrowMax).minZ(newWideMin).maxZ(newWideMax);
    } else {
      return box.minZ(newNarrowMin).maxZ(newNarrowMax).minX(newWideMin).maxX(newWideMax);
    }
  }
}
