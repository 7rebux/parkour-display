package pw.rebux.parkourdisplay.core.run.split;

import static net.labymod.api.client.component.Component.text;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.BoundingBoxUtils;
import pw.rebux.parkourdisplay.core.util.TickFormatter;

@Data
@RequiredArgsConstructor
public final class Split {

  private final String label;
  private final AxisAlignedBoundingBox boundingBox;
  private final SplitBoxTriggerMode triggerMode;

  private Long personalBest;
  private transient boolean passed;
  private transient Long lastTicks;
  private transient Long lastDelta;

  public boolean intersects(AxisAlignedBoundingBox other) {
    return switch (this.triggerMode) {
      case Intersect -> this.boundingBox.intersects(other);
      case IntersectXZAboveY -> BoundingBoxUtils.intersectsXZAboveY(this.boundingBox, other);
      case IntersectXZSameY -> BoundingBoxUtils.intersectsXZSameY(this.boundingBox, other);
    };
  }

  public void updatePB(ParkourDisplayAddon addon, long ticks) {
    var color = NamedTextColor.GRAY;
    var delta = 0L;

    if (personalBest == null) {
      this.personalBest = ticks;
    } else {
      delta = ticks - this.personalBest;

      if (delta < 0) {
        this.personalBest = ticks;
        color = NamedTextColor.GREEN;
      } else if (delta > 0) {
        color = NamedTextColor.RED;
      }
    }

    this.lastTicks = ticks;
    this.lastDelta = delta;

    if (addon.configuration().showRunSplitsInChat().get()) {
      var formattedTicks = addon.configuration().formatTicks().get()
          ? TickFormatter.formatTicks(ticks)
          : String.valueOf(ticks);
      var formattedDelta = addon.configuration().formatTicks().get()
          ? delta > 0 ? "+" + TickFormatter.formatTicks(delta) : TickFormatter.formatTicks(delta)
          : delta > 0 ? "+" + delta : String.valueOf(delta);

      addon.displayMessageWithPrefix(
          text("%s: %s (%s)".formatted(label, formattedTicks, formattedDelta), color));
    }
  }
}
