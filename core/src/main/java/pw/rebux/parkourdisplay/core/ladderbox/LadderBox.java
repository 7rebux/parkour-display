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
  private final AxisAlignedBoundingBox intersectionBox =
      expand(this.boundingBox);

  private AxisAlignedBoundingBox expand(AxisAlignedBoundingBox boundingBox) {
    if (boundingBox.getXWidth() == 1.0D) {
      return boundingBox
          .minX(boundingBox.getMinX() + 0.3)
          .maxX(boundingBox.getMaxX() - 0.3)
          .minZ(boundingBox.getMaxZ())
          .maxZ(boundingBox.getMinZ() + 0.7);
    } else {
      return boundingBox
          .minZ(boundingBox.getMinZ() + 0.3)
          .maxZ(boundingBox.getMaxZ() - 0.3)
          .minX(boundingBox.getMaxX())
          .maxX(boundingBox.getMinX() + 0.7);
    }
  }
}
