package pw.rebux.parkourdisplay.core.run.split;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.state.PositionOffset;
import pw.rebux.parkourdisplay.core.util.TickFormatter;

@Data
@RequiredArgsConstructor
public final class RunSplit {

  private final String label;
  private final PositionOffset positionOffset;

  private boolean passed;
  private Long personalBest;
  private Long lastTicks;
  private Long lastDelta;

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
      var formattedTicks = addon.configuration().formatRunSplits().get()
          ? TickFormatter.formatTicks(ticks)
          : String.valueOf(ticks);
      var formattedDelta = addon.configuration().formatRunSplits().get()
          ? delta > 0 ? "+" + TickFormatter.formatTicks(delta) : TickFormatter.formatTicks(delta)
          : delta > 0 ? "+" + delta : String.valueOf(delta);

      addon.displayMessage(
          Component.text("%s: %s (%s)".formatted(label, formattedTicks, formattedDelta), color));
    }
  }
}
