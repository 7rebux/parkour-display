package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.PreturnWidget.PreturnWidgetConfig;

/// The yaw delta on the tick before jump initiation.
public final class PreturnWidget extends TextHudWidget<PreturnWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;
  private String stringFormat;

  private float secondLastYaw = 0;

  public PreturnWidget(ParkourDisplayAddon addon) {
    super("preturn", PreturnWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(PreturnWidgetConfig config) {
    super.load(config);

    this.textLine = this.createLine(translatable("parkourdisplay.labels.preturn"), 0);
    this.stringFormat = "%%.%df".formatted(config.decimalPlaces().get());
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var state = this.addon.playerState();

    if (state.isJumpTick()) {
      var turn = String.format(this.stringFormat, state.lastTick().yaw() - this.secondLastYaw);
      this.textLine.updateAndFlush(turn);
    }

    this.secondLastYaw = state.lastTick().yaw();
  }

  @Getter
  public static class PreturnWidgetConfig extends TextHudWidgetConfig {

    @SliderSetting(min = 0, max = 10)
    private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
  }
}
