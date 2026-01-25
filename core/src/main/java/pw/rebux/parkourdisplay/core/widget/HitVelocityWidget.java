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
import pw.rebux.parkourdisplay.core.widget.HitVelocityWidget.HitVelocityWidgetConfig;

public class HitVelocityWidget extends TextHudWidget<HitVelocityWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private final TextLine[] textLines = new TextLine[2];
  private String stringFormat;

  public HitVelocityWidget(ParkourDisplayAddon addon) {
    super("hit_velocity", HitVelocityWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(HitVelocityWidgetConfig config) {
    super.load(config);

    if (config.singleLine.get()) {
      this.textLines[0] = createLine(
          translatable("parkourdisplay.labels.hit_velocity.single"),
          "%s %s".formatted(0, 0));
    } else {
      this.textLines[0] = createLine(translatable("parkourdisplay.labels.hit_velocity.x"), 0);
      this.textLines[1] = createLine(translatable("parkourdisplay.labels.hit_velocity.z"), 0);
    }

    this.stringFormat = "%%.%df".formatted(config.decimalPlaces().get());
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var parkourState = this.addon.playerParkourState();
    var x = String.format(this.stringFormat, parkourState.hitVelocityX());
    var z = String.format(this.stringFormat, parkourState.hitVelocityZ());

    if (this.config.singleLine().get()) {
      this.textLines[0].updateAndFlush("%s %s".formatted(x, z));
    } else {
      this.textLines[0].updateAndFlush(x);
      this.textLines[1].updateAndFlush(z);
    }
  }

  @Getter
  public static class HitVelocityWidgetConfig extends TextHudWidgetConfig {

    @SwitchSetting
    private final ConfigProperty<Boolean> singleLine = new ConfigProperty<>(false);

    @SliderSetting(min = 0, max = 10)
    private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
  }
}
