package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingRequires;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.AirTimeWidget.AirTimeWidgetConfig;

public class AirTimeWidget extends TextHudWidget<AirTimeWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;
  private long lastValue = -1;
  private int streak = 1;

  public AirTimeWidget(ParkourDisplayAddon addon) {
    super("air_time", AirTimeWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(AirTimeWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(translatable("parkourdisplay.labels.air_time"), 0);
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var state = this.addon.playerState();
    var airTime = state.airTime();
    var shouldUpdate = this.config.incremental().get() || state.currentTick().onGround();

    if (airTime <= 0 || !shouldUpdate) {
      return;
    }

    streak = (airTime == lastValue) ? streak + 1 : 1;
    lastValue = airTime;

    this.textLine.updateAndFlush(
        this.config.showStreak().get() && streak > 1
            ? "%d (x%d)".formatted(airTime, streak)
            : airTime
    );
  }

  @Getter
  public static class AirTimeWidgetConfig extends TextHudWidgetConfig {

    /**
     * Whether to update the label continuously while airborne.
     */
    @SwitchSetting
    private final ConfigProperty<Boolean> incremental = new ConfigProperty<>(true);

    /**
     * Whether to show the streak count in the label.
     */
    @SettingRequires(value = "incremental", invert = true)
    @SwitchSetting
    private final ConfigProperty<Boolean> showStreak = new ConfigProperty<>(false);
  }
}
