package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.JumpCoordinatesWidget.JumpCoordinatesWidgetConfig;

public class JumpCoordinatesWidget extends TextHudWidget<JumpCoordinatesWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private final TextLine[] textLines = new TextLine[3];
  private String stringFormat;

  public JumpCoordinatesWidget(ParkourDisplayAddon addon) {
    super("jump_coordinates", JumpCoordinatesWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(JumpCoordinatesWidgetConfig config) {
    super.load(config);

    if (config.singleLine().get()) {
      this.textLines[0] = createLine(
          translatable("parkourdisplay.labels.jump_coordinates.single"),
          "%s %s %s".formatted(0, 0, 0));
    } else {
      this.textLines[0] = createLine(translatable("parkourdisplay.labels.jump_coordinates.x"), 0);
      this.textLines[1] = createLine(translatable("parkourdisplay.labels.jump_coordinates.y"), 0);
      this.textLines[2] = createLine(translatable("parkourdisplay.labels.jump_coordinates.z"), 0);
    }

    this.stringFormat = "%%.%df".formatted(config.decimalPlaces().get());
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var parkourState = this.addon.playerParkourState();
    var x = String.format(this.stringFormat, parkourState.jumpX());
    var y = String.format(this.stringFormat, parkourState.jumpY());
    var z = String.format(this.stringFormat, parkourState.jumpZ());

    if (this.config.singleLine().get()) {
      this.textLines[0].updateAndFlush("%s %s %s".formatted(x, y, z));
    } else {
      this.textLines[0].updateAndFlush(x);
      this.textLines[1].updateAndFlush(y);
      this.textLines[2].updateAndFlush(z);
    }
  }

  @Accessors(fluent = true)
  @Getter
  public static class JumpCoordinatesWidgetConfig extends TextHudWidgetConfig {

    @SwitchSetting
    private final ConfigProperty<Boolean> singleLine = new ConfigProperty<>(false);

    @SliderSetting(min = 0, max = 10)
    private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
  }
}
