package pw.rebux.parkourdisplay.common.run;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import pw.rebux.parkourdisplay.common.geom.Aabb;
import pw.rebux.parkourdisplay.common.platform.Message;
import pw.rebux.parkourdisplay.common.platform.ParkourContext;
import pw.rebux.parkourdisplay.common.platform.TextColorType;
import pw.rebux.parkourdisplay.common.util.BoundingBoxUtils;
import pw.rebux.parkourdisplay.common.util.TickFormatter;

@Data
@RequiredArgsConstructor
public class RunSplit {

  private final String label;
  private final Aabb boundingBox;
  private final SplitBoxTriggerMode triggerMode;

  private Long personalBest;
  private transient boolean passed;
  private transient Long lastTicks;
  private transient Long lastDelta;

  public boolean intersects(Aabb other) {
    return switch (this.triggerMode) {
      case Intersect -> this.boundingBox.intersects(other);
      case IntersectXZAboveY -> BoundingBoxUtils.intersectsXZAboveY(this.boundingBox, other);
      case IntersectXZSameY -> BoundingBoxUtils.intersectsXZSameY(this.boundingBox, other);
    };
  }

  public void updatePB(ParkourContext context, long ticks) {
    var color = TextColorType.GRAY;
    var delta = 0L;

    if (personalBest == null) {
      this.personalBest = ticks;
    } else {
      delta = ticks - this.personalBest;

      if (delta < 0) {
        this.personalBest = ticks;
        color = TextColorType.GREEN;
      } else if (delta > 0) {
        color = TextColorType.RED;
      }
    }

    this.lastTicks = ticks;
    this.lastDelta = delta;

    if (context.config().showRunSplitsInChat()) {
      var formattedTicks = TickFormatter.format(ticks, context.config().formatTicks());
      var formattedDelta = TickFormatter.format(delta, context.config().formatTicks());

      context.chat().display(
          Message.of("messages.run.splitHit")
              .arg(label)
              .arg(formattedTicks)
              .arg(delta > 0 ? "+" + formattedDelta : formattedDelta)
              .color(color));
    }
  }
}
