package pw.rebux.parkourdisplay.core;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.color.ColorPickerWidget.ColorPickerSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingSection;
import net.labymod.api.util.Color;

@ConfigName("settings")
@Getter
@Accessors(fluent = true)
public class ParkourDisplayConfiguration extends AddonConfig {

  @SwitchSetting
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> showGroundDurations = new ConfigProperty<>(false);

  @SwitchSetting
  private final ConfigProperty<Boolean> showJumpDurations = new ConfigProperty<>(true);

  @SettingSection("landingBlock")
  @SliderSetting(min = 0, max = 10)
  private final ConfigProperty<Integer> landingBlockOffsetDecimalPlaces =
      new ConfigProperty<>(3);

  @SwitchSetting
  private final ConfigProperty<Boolean> showLandingBlockOffsets =
      new ConfigProperty<>(false);

  @SwitchSetting
  private final ConfigProperty<Boolean> highlightLandingBlocks =
      new ConfigProperty<>(true);

  @ColorPickerSetting(alpha = true, chroma = true)
  private final ConfigProperty<Color> landingBlockFillColor =
      new ConfigProperty<>(Color.ofRGB(0, 192, 255, 25));

  @ColorPickerSetting(alpha = true, chroma = true)
  private final ConfigProperty<Color> landingBlockOutlineColor =
      new ConfigProperty<>(Color.ofRGB(0, 192, 255, 75));

  @SliderSetting(min = 0.01F, max = 1.0F, steps = 0.01F)
  private final ConfigProperty<Float> landingBlockOutlineThickness =
      new ConfigProperty<>(0.01F);
}
