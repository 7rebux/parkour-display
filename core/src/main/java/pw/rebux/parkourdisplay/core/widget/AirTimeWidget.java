package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.AirTimeWidget.AirTimeWidgetConfig;

public class AirTimeWidget extends TextHudWidget<AirTimeWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;

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
    var shouldUpdate = state.airTicks() > 0
        && (this.config.incremental().get() || state.currentTick().onGround());

    if (shouldUpdate) {
      this.textLine.updateAndFlush(state.airTicks());
    }
  }

  @Getter
  public static class AirTimeWidgetConfig extends TextHudWidgetConfig {

    /**
     * Whether to update the air time label incrementally while airborne.
     */
    @SwitchSetting
    private final ConfigProperty<Boolean> incremental = new ConfigProperty<>(true);
  }
}
