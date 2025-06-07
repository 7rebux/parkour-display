package pw.rebux.parkourdisplay.core.widget.config;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;

@Accessors(fluent = true)
@Getter
public class JumpAngleWidgetConfig extends TextHudWidgetConfig {

  @SliderSetting(min = 0, max = 10)
  private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
}
