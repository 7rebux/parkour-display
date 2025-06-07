package pw.rebux.parkourdisplay.core.widget;

import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.config.LastTimingWidgetConfig;

public class LastTimingWidget extends TextHudWidget<LastTimingWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;

  public LastTimingWidget(ParkourDisplayAddon addon) {
    super("last_timing", LastTimingWidgetConfig.class);

    this.bindCategory(addon.category());

    this.addon = addon;
  }

  @Override
  public void load(LastTimingWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(Component.translatable("parkourdisplay.labels.last_timing"), "");
  }
}
