package pw.rebux.parkourdisplay.core;

import static net.labymod.api.client.component.Component.space;
import static net.labymod.api.client.component.Component.text;

import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.util.math.AxisAlignedBoundingBox;

// TODO: Landing and Hit mode
public class LandingBlockOffsets {

  private Double bestOffset;

  private Double tempTotalOffset;
  private Double tempXOffset;
  private Double tempZOffset;

  public void compute(
      ClientPlayer player,
      AxisAlignedBoundingBox box,
      double x,
      double y,
      double z,
      double lastX,
      double lastY,
      double lastZ
  ) {
    var xOffset = checkX(player, box, x);
    var zOffset = checkZ(player, box, z);
    var totalOffset = calculateTotalOffset(xOffset, zOffset);

    if (tempTotalOffset == null || totalOffset > tempTotalOffset) {
      tempTotalOffset = totalOffset;
      tempXOffset = xOffset;
      tempZOffset = zOffset;
    }
  }

  public void update(ParkourDisplayAddon addon) {
    if (tempTotalOffset == null || tempXOffset == null || tempZOffset == null) {
      return;
    }

    var format = "%%.%df".formatted(addon.configuration().landingBlockOffsetDecimalPlaces().get());

    addon.playerParkourState().lastTotalLandingBlockOffset(tempTotalOffset);
    addon.playerParkourState().lastLandingBlockOffsetX(tempXOffset);
    addon.playerParkourState().lastLandingBlockOffsetZ(tempZOffset);

    if (bestOffset == null || tempTotalOffset > bestOffset) {
      bestOffset = tempTotalOffset;

      // Always show new pb in chat
      addon.displayMessage(
          text("New PB:")
              .append(space())
              .append(text(String.format(format, tempTotalOffset))));
    } else if (addon.configuration().showLandingBlockOffsets().get()) {
      addon.displayMessage(
          text("X Offset:")
              .append(space())
              .append(text(String.format(format, tempXOffset))));
      addon.displayMessage(
          text("Z Offset:")
              .append(space())
              .append(text(String.format(format, tempZOffset))));
    }

    tempTotalOffset = null;
    tempXOffset = null;
    tempZOffset = null;
  }

  private double checkX(ClientPlayer player, AxisAlignedBoundingBox box, double x) {
    var halfPlayerWidth = player.axisAlignedBoundingBox().getXWidth() / 2;
    var left = box.getMaxX() - x + halfPlayerWidth;
    var right = x - box.getMinX() + halfPlayerWidth;

    return Math.min(left, right);
  }

  private double checkZ(ClientPlayer player, AxisAlignedBoundingBox box, double z) {
    var halfPlayerWidth = player.axisAlignedBoundingBox().getZWidth() / 2;
    var front = box.getMaxZ() - z + halfPlayerWidth;
    var back = z - box.getMinZ() + halfPlayerWidth;

    return Math.min(front, back);
  }

  private double calculateTotalOffset(double xOffset, double zOffset) {
    if (xOffset < 0 && zOffset < 0) {
      return -Math.hypot(xOffset, zOffset);
    } else if (xOffset < 0) {
      return xOffset;
    } else if (zOffset < 0) {
      return zOffset;
    } else {
      return Math.hypot(xOffset, zOffset);
    }
  }
}
