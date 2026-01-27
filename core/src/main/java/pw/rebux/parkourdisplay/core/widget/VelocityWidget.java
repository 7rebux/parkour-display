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
import pw.rebux.parkourdisplay.core.widget.VelocityWidget.VelocityWidgetConfig;

public class VelocityWidget extends TextHudWidget<VelocityWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private final TextLine[] textLines = new TextLine[3];
  private String stringFormat;

  public VelocityWidget(ParkourDisplayAddon addon) {
    super("velocity", VelocityWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(VelocityWidgetConfig config) {
    super.load(config);

    if (config.singleLine().get()) {
      this.textLines[0] = createLine(
          translatable("parkourdisplay.labels.velocity.single"),
          "%s %s %s".formatted(0, 0, 0));
    } else {
      this.textLines[0] = createLine(translatable("parkourdisplay.labels.velocity.x"), 0);
      this.textLines[1] = createLine(translatable("parkourdisplay.labels.velocity.y"), 0);
      this.textLines[2] = createLine(translatable("parkourdisplay.labels.velocity.z"), 0);
    }

    this.stringFormat = "%%.%df".formatted(config.decimalPlaces().get());
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var state = this.addon.playerState();
    var vx = String.format(this.stringFormat, state.currentTick().x() - state.lastTick().x());
    var vy = String.format(this.stringFormat, state.currentTick().y() - state.lastTick().y());
    var vz = String.format(this.stringFormat, state.currentTick().z() - state.lastTick().z());

    if (this.config.singleLine().get()) {
      this.textLines[0].updateAndFlush("%s %s %s".formatted(vx, vy, vz));
    } else {
      this.textLines[0].updateAndFlush(vx);
      this.textLines[1].updateAndFlush(vy);
      this.textLines[2].updateAndFlush(vz);
    }
  }

  @Getter
  public static class VelocityWidgetConfig extends TextHudWidgetConfig {

    @SwitchSetting
    private final ConfigProperty<Boolean> singleLine = new ConfigProperty<>(false);

    @SliderSetting(min = 0, max = 10)
    private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
  }
}
