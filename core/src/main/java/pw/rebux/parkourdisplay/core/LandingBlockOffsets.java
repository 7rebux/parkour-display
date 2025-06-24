package pw.rebux.parkourdisplay.core;

import static net.labymod.api.client.component.Component.space;
import static net.labymod.api.client.component.Component.text;

import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.util.math.AxisAlignedBoundingBox;

public class LandingBlockOffsets {

  private Double bestOffset;

  private Double tempTotalOffset;
  private Double tempXOffset;
  private Double tempZOffset;

  public void compute(ClientPlayer player, AxisAlignedBoundingBox box, double x, double z) {
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
          text("New PB:", NamedTextColor.GREEN)
              .append(space())
              .append(text(String.format(format, tempTotalOffset), NamedTextColor.DARK_GREEN)));
    } else if (addon.configuration().showLandingBlockOffsets().get()) {
      addon.displayMessage(
          text("X Offset:", NamedTextColor.RED)
              .append(space())
              .append(text(String.format(format, tempXOffset), NamedTextColor.DARK_RED)));
      addon.displayMessage(
          text("Z Offset:", NamedTextColor.RED)
              .append(space())
              .append(text(String.format(format, tempZOffset), NamedTextColor.DARK_RED)));
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
