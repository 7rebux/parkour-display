package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.LastTimingWidget.LastTimingWidgetConfig;

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

    this.textLine = createLine(translatable("parkourdisplay.labels.last_timing"), "-");
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var lastTiming = this.addon.playerParkourState().lastTiming();

    this.textLine.updateAndFlush(lastTiming);
  }

  @Accessors(fluent = true)
  @Getter
  public static class LastTimingWidgetConfig extends TextHudWidgetConfig {

    // TODO: Implement
    @SwitchSetting
    private final ConfigProperty<Boolean> showMillis = new ConfigProperty<>(true);
  }
}
