package pw.rebux.parkourdisplay.core.state;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;

@Builder
@Getter
@Accessors(fluent = true)
public class PositionOffset {

  private final double posX;
  private final double posY;
  private final double posZ;
  private final double offsetX;
  private final double offsetZ;

  @Getter(lazy = true)
  private final AxisAlignedBoundingBox boundingBox =
      new AxisAlignedBoundingBox(
          -offsetX / 2,
          -0,
          -offsetZ / 2,
          offsetX / 2,
          0,
          offsetZ / 2
      );

  @Getter(lazy = true)
  private final DoubleVector3 positionVector = new DoubleVector3(posX, posY, posZ);
}
