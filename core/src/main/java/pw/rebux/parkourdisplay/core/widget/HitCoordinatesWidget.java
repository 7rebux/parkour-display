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
import pw.rebux.parkourdisplay.core.widget.HitCoordinatesWidget.HitCoordinatesWidgetConfig;

public class HitCoordinatesWidget extends TextHudWidget<HitCoordinatesWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private final TextLine[] textLines = new TextLine[3];
  private String stringFormat;

  public HitCoordinatesWidget(ParkourDisplayAddon addon) {
    super("hit_coordinates", HitCoordinatesWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(HitCoordinatesWidgetConfig config) {
    super.load(config);

    if (config.singleLine().get()) {
      this.textLines[0] = createLine(
          translatable("parkourdisplay.labels.hit_coordinates.single"),
          "%s %s %s".formatted(0, 0, 0));
    } else {
      this.textLines[0] = createLine(translatable("parkourdisplay.labels.hit_coordinates.x"), 0);
      this.textLines[1] = createLine(translatable("parkourdisplay.labels.hit_coordinates.y"), 0);
      this.textLines[2] = createLine(translatable("parkourdisplay.labels.hit_coordinates.z"), 0);
    }

    this.stringFormat = "%%.%df".formatted(config.decimalPlaces().get());
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var state = this.addon.playerState();

    if (!state.isLandTick()) {
      return;
    }

    var x = String.format(this.stringFormat, state.currentTick().x());
    var y = String.format(this.stringFormat, state.currentTick().y());
    var z = String.format(this.stringFormat, state.currentTick().z());

    if (this.config.singleLine().get()) {
      this.textLines[0].updateAndFlush("%s %s %s".formatted(x, y, z));
    } else {
      this.textLines[0].updateAndFlush(x);
      this.textLines[1].updateAndFlush(y);
      this.textLines[2].updateAndFlush(z);
    }
  }

  @Getter
  public static class HitCoordinatesWidgetConfig extends TextHudWidgetConfig {

    @SwitchSetting
    private final ConfigProperty<Boolean> singleLine = new ConfigProperty<>(false);

    @SliderSetting(min = 0, max = 10)
    private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
  }
}
