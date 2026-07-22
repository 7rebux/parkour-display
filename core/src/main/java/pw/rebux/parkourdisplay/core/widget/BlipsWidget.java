package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.BlipsWidget.BlipsWidgetConfig;

/// [Blips - MCPK Wiki](https://www.mcpk.wiki/wiki/Blip)
public class BlipsWidget extends TextHudWidget<BlipsWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;
  private String stringFormat;

  private long chained = 0;

  private boolean secondLastOnGround = false;
  private double lastVy = 0;

  public BlipsWidget(ParkourDisplayAddon addon) {
    super("blips", BlipsWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(BlipsWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(translatable("parkourdisplay.labels.blips"), "-");
    this.stringFormat = "%%.%df".formatted(config.decimalPlaces().get());
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var state = this.addon.playerState();

    if (state.lastTick().onGround()) {
      if (!secondLastOnGround
          && lastVy == 0
          && (state.lastTick().y() % 0.015625 != 0)
          && state.airTime() > 0
      ) {
        var height = String.format(this.stringFormat, state.lastTick().y());
        if (++chained == 1) {
          this.textLine.updateAndFlush("%d (Y: %s)".formatted(chained, height));
        } else {
          this.textLine.updateAndFlush("%d chained (Y: %s)".formatted(chained, height));
        }
      } else {
        this.chained = 0;
        this.textLine.updateAndFlush("-");
      }
    }

    this.secondLastOnGround = state.lastTick().onGround();
    this.lastVy = state.vy();
  }

  @Getter
  public static class BlipsWidgetConfig extends TextHudWidgetConfig {

    @SliderSetting(min = 0, max = 10)
    private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
  }
}
