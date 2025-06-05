package pw.rebux.parkourdisplay.core.widget;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.LastTimingWidget.LastTimingWidgetConfig;

public class LastTimingWidget extends TextHudWidget<LastTimingWidgetConfig> {

  private final ParkourDisplayAddon addon;

  public LastTimingWidget(ParkourDisplayAddon addon) {
    super("last_timing");

    this.addon = addon;
  }

  public static class LastTimingWidgetConfig extends TextHudWidgetConfig {

    @SwitchSetting
    private final ConfigProperty<Boolean> showMillis = new ConfigProperty<>(true);

    public ConfigProperty<Boolean> showMillis() {
      return showMillis;
    }
  }
}
