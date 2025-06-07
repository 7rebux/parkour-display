package pw.rebux.parkourdisplay.core;

import net.labymod.api.util.math.AxisAlignedBoundingBox;

public class LandingBlockOffsets {

  private Double lastOffsetX, lastOffsetZ;
  private Double bestOffset;

  public void update(AxisAlignedBoundingBox box, double x, double y, double z, double lastX, double lastY, double lastZ) {
    if (y < box.getMinY()) {
      return;
    }


  }
}
