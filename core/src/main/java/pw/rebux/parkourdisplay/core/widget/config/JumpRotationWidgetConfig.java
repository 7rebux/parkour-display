package pw.rebux.parkourdisplay.core.widget.config;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;

@Accessors(fluent = true)
public class JumpRotationWidgetConfig extends TextHudWidgetConfig {

  @Getter
  @SwitchSetting
  private final ConfigProperty<Boolean> singleLine = new ConfigProperty<>(false);

  @Getter
  @SliderSetting(min = 0, max = 10)
  private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
}
