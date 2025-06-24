package pw.rebux.parkourdisplay.core;

import static net.labymod.api.client.component.Component.space;
import static net.labymod.api.client.component.Component.text;

import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.util.math.AxisAlignedBoundingBox;

// TODO: Landing and Hit mode
public class LandingBlockOffsets {

  private Double bestOffset;

  public void update(
      ParkourDisplayAddon addon,
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

    var format = "%%.%df"
        .formatted(addon.configuration().landingBlockOffsetDecimalPlaces().get());

    addon.playerParkourState().lastTotalLandingBlockOffset(totalOffset);
    addon.playerParkourState().lastLandingBlockOffsetX(xOffset);
    addon.playerParkourState().lastLandingBlockOffsetZ(zOffset);

    if (bestOffset == null || totalOffset > bestOffset) {
      bestOffset = totalOffset;

      // Always show new pb in chat
      addon.displayMessage(
          text("New PB:")
              .append(space())
              .append(text(String.format(format, bestOffset))));
    } else if (addon.configuration().showLandingBlockOffsets().get()) {
      addon.displayMessage(
          text("X Offset:")
              .append(space())
              .append(text(String.format(format, xOffset))));
      addon.displayMessage(
          text("Z Offset:")
              .append(space())
              .append(text(String.format(format, zOffset))));
    }
  }

  private double checkX(ClientPlayer player, AxisAlignedBoundingBox box, double x) {
    var halfPlayerWidth = player.axisAlignedBoundingBox().getXWidth() / 2;
    var left = box.getMaxX() - x + halfPlayerWidth;
    var right = x - box.getMinX() + halfPlayerWidth;

    return Math.max(left, right);
  }

  private double checkZ(ClientPlayer player, AxisAlignedBoundingBox box, double z) {
    var halfPlayerWidth = player.axisAlignedBoundingBox().getZWidth() / 2;
    var front = box.getMaxZ() - z + halfPlayerWidth;
    var back = z - box.getMinZ() + halfPlayerWidth;

    return Math.max(front, back);
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
