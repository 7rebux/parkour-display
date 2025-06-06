package pw.rebux.parkourdisplay.core.widget.config;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;

@Accessors(fluent = true)
public class LastTimingWidgetConfig extends TextHudWidgetConfig {

  @Getter
  @SwitchSetting
  private final ConfigProperty<Boolean> showMillis = new ConfigProperty<>(true);
}
