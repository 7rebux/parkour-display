package pw.rebux.parkourdisplay.core.state;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@Data
@RequiredArgsConstructor
@Accessors(fluent = true)
public final class RunSplit {

  private final PositionOffset positionOffset;

  private boolean passed;
  private Integer personalBest;

  // TODO: Logic should not be in here
  public void updatePB(ParkourDisplayAddon addon, int ticks) {
    var color = NamedTextColor.GRAY;
    var delta = 0;

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

    addon.displayMessage(Component.text("Split: %d (%d)".formatted(ticks, delta), color));
  }
}
