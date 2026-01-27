package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.SpeedVectorWidget.SpeedVectorWidgetConfig;

public class SpeedVectorWidget extends TextHudWidget<SpeedVectorWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private final TextLine[] textLines = new TextLine[2];
  private String stringFormat;

  public SpeedVectorWidget(ParkourDisplayAddon addon) {
    super("speed_vector", SpeedVectorWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(SpeedVectorWidgetConfig config) {
    super.load(config);

    if (config.singleLine().get()) {
      this.textLines[0] = createLine(
          translatable("parkourdisplay.labels.speed_vector.single"),
          "%s / %s".formatted(0, 0));
    } else {
      this.textLines[0] = createLine(translatable("parkourdisplay.labels.speed_vector.speed"), 0);
      this.textLines[1] = createLine(translatable("parkourdisplay.labels.speed_vector.angle"), 0);
    }

    this.stringFormat = "%%.%df".formatted(config.decimalPlaces().get());
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var state = this.addon.playerState();
    // TODO: These values are used in multiple places, might put them in PlayerState
    var vx = state.currentTick().x() - state.lastTick().x();
    var vz = state.currentTick().z() - state.lastTick().z();
    var speed = Math.hypot(vx, vz);
    var angle = Math.toDegrees(Math.atan2(vx == 0 ? 0 : -vx, vz));

    if (this.config.singleLine().get()) {
      this.textLines[0].updateAndFlush("%s / %s".formatted(speed, angle));
    } else {
      this.textLines[0].updateAndFlush(String.format(this.stringFormat, speed));
      this.textLines[1].updateAndFlush(String.format(this.stringFormat, angle));
    }
  }

  @Getter
  public static class SpeedVectorWidgetConfig extends TextHudWidgetConfig {

    @SwitchSetting
    private final ConfigProperty<Boolean> singleLine = new ConfigProperty<>(false);

    @SliderSetting(min = 0, max = 10)
    private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
  }
}
