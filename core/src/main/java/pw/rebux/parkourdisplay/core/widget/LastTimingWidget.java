package pw.rebux.parkourdisplay.core.widget;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.config.LastTimingWidgetConfig;

public class LastTimingWidget extends TextHudWidget<LastTimingWidgetConfig> {

  private final ParkourDisplayAddon addon;

  public LastTimingWidget(ParkourDisplayAddon addon) {
    super("last_timing", LastTimingWidgetConfig.class);

    this.addon = addon;
  }

  @Override
  public void load(LastTimingWidgetConfig config) {
    super.load(config);
  }
}
