package pw.rebux.parkourdisplay.core.splits;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.state.PositionOffset;
import pw.rebux.parkourdisplay.core.util.TickFormatter;

@Data
@RequiredArgsConstructor
@Accessors(fluent = true)
public final class RunSplit {

  private final PositionOffset positionOffset;

  private boolean passed;
  private Long personalBest;
  private Long lastTicks;

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

    if (addon.configuration().showRunSplitsInChat().get()) {
      var formattedTicks = addon.configuration().formatRunSplits().get()
          ? TickFormatter.formatTicks(ticks)
          : String.valueOf(ticks);
      var formattedDelta = addon.configuration().formatRunSplits().get()
          ? TickFormatter.formatTicks(delta)
          : delta > 0 ? "+" + delta : String.valueOf(delta);

      addon.displayMessage(
          Component.text("Split: %s (%s)".formatted(formattedTicks, formattedDelta), color));
    }
  }
}
