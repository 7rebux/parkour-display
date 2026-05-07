package pw.rebux.parkourdisplay.core;

import lombok.Getter;
import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.color.ColorPickerWidget.ColorPickerSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.dropdown.DropdownWidget.DropdownSetting;
import net.labymod.api.configuration.loader.Config;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.annotation.ShowSettingInParent;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingSection;
import net.labymod.api.util.Color;
import pw.rebux.parkourdisplay.core.macro.MacroRotationChange;

@ConfigName("settings")
@Getter
public final class ParkourDisplayConfiguration extends AddonConfig {

  @SwitchSetting
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> showGroundDurations = new ConfigProperty<>(false);

  @SwitchSetting
  private final ConfigProperty<Boolean> showJumpDurations = new ConfigProperty<>(true);

  @SettingSection("formatting")
  @SliderSetting(min = 0, max = 10)
  private final ConfigProperty<Integer> chatDecimalPlaces = new ConfigProperty<>(3);

  @SwitchSetting
  private final ConfigProperty<Boolean> formatTicks = new ConfigProperty<>(true);

  @SettingSection("landingBlock")
  @SwitchSetting
  private final ConfigProperty<Boolean> showLandingBlockOffsets =
      new ConfigProperty<>(false);

  private final HighlightLandingBlocksSettings highlightLandingBlocksSettings =
      new HighlightLandingBlocksSettings();

  @SettingSection("runSplit")
  @SwitchSetting
  private final ConfigProperty<Boolean> showRunSplitsInChat = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> showRunFinishOffsets = new ConfigProperty<>(false);

  @SwitchSetting
  private final ConfigProperty<Boolean> importLandingBlocks = new ConfigProperty<>(true);

  private final HighlightRunSplitsSettings highlightRunSplitsSettings =
      new HighlightRunSplitsSettings();

  private final HighlightRunTickStatesSettings highlightRunTickStates =
      new HighlightRunTickStatesSettings();

  @SettingSection("macro")
  @SwitchSetting
  private final ConfigProperty<Boolean> unpressKeys = new ConfigProperty<>(true);

  @DropdownSetting
  private final ConfigProperty<MacroRotationChange> rotationChange =
      new ConfigProperty<>(MacroRotationChange.Absolute);

  @Getter
  public static final class HighlightLandingBlocksSettings extends Config {

    @SwitchSetting
    @ShowSettingInParent
    private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

    @ColorPickerSetting(alpha = true, chroma = true)
    private final ConfigProperty<Color> fillColor =
        new ConfigProperty<>(Color.ofRGB(0, 192, 255, 25));

    @ColorPickerSetting(alpha = true, chroma = true)
    private final ConfigProperty<Color> outlineColor =
        new ConfigProperty<>(Color.ofRGB(0, 192, 255, 75));

    @SliderSetting(min = 1, max = 30)
    private final ConfigProperty<Integer> outlineThickness = new ConfigProperty<>(10);
  }

  @Getter
  public static final class HighlightRunSplitsSettings extends Config {

    @SwitchSetting
    private final ConfigProperty<Boolean> highlightRegularSplits =
        new ConfigProperty<>(false);

    @SwitchSetting
    private final ConfigProperty<Boolean> highlightEndSplit = new ConfigProperty<>(true);

    @ColorPickerSetting(alpha = true, chroma = true)
    private final ConfigProperty<Color> regularFillColor =
        new ConfigProperty<>(Color.ofRGB(255, 0, 0, 30));

    @ColorPickerSetting(alpha = true, chroma = true)
    private final ConfigProperty<Color> regularOutlineColor =
        new ConfigProperty<>(Color.ofRGB(255, 0, 0, 100));

    @ColorPickerSetting(alpha = true, chroma = true)
    private final ConfigProperty<Color> intersectingFillColor =
        new ConfigProperty<>(Color.ofRGB(0, 255, 0, 30));

    @ColorPickerSetting(alpha = true, chroma = true)
    private final ConfigProperty<Color> intersectingOutlineColor =
        new ConfigProperty<>(Color.ofRGB(0, 255, 0, 100));

    @SliderSetting(min = 1, max = 30)
    private final ConfigProperty<Integer> outlineThickness = new ConfigProperty<>(5);
  }

  @Getter
  public static final class HighlightRunTickStatesSettings extends Config {

    @SwitchSetting
    @ShowSettingInParent
    private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(false);

    @ColorPickerSetting(alpha = true, chroma = true)
    private final ConfigProperty<Color> startFillColor =
        new ConfigProperty<>(Color.ofRGB(0, 255, 0, 30));

    @ColorPickerSetting(alpha = true, chroma = true)
    private final ConfigProperty<Color> startOutlineColor =
        new ConfigProperty<>(Color.ofRGB(0, 255, 0, 100));

    @ColorPickerSetting(alpha = true, chroma = true)
    private final ConfigProperty<Color> regularFillColor =
        new ConfigProperty<>(Color.ofRGB(255, 0, 0, 30));

    @ColorPickerSetting(alpha = true, chroma = true)
    private final ConfigProperty<Color> regularOutlineColor =
        new ConfigProperty<>(Color.ofRGB(255, 0, 0, 100));

    @ColorPickerSetting(alpha = true, chroma = true)
    private final ConfigProperty<Color> onGroundFillColor =
        new ConfigProperty<>(Color.ofRGB(255, 255, 0, 30));

    @ColorPickerSetting(alpha = true, chroma = true)
    private final ConfigProperty<Color> onGroundOutlineColor =
        new ConfigProperty<>(Color.ofRGB(255, 255, 0, 100));

    @SliderSetting(min = 1, max = 30)
    private final ConfigProperty<Integer> outlineThickness = new ConfigProperty<>(5);
  }
}
