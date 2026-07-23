package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.SecondTurnWidget.SecondTurnWidgetConfig;

/// The yaw delta at the tick exactly 1t after jump initiation.
public final class SecondTurnWidget extends TextHudWidget<SecondTurnWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;
  private String stringFormat;

  private boolean jumpedLastTick = false;

  public SecondTurnWidget(ParkourDisplayAddon addon) {
    super("second_turn", SecondTurnWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(SecondTurnWidgetConfig config) {
    super.load(config);

    this.textLine = this.createLine(translatable("parkourdisplay.labels.second_turn"), 0);
    this.stringFormat = "%%.%df".formatted(config.decimalPlaces().get());
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var state = this.addon.playerState();

    if (this.jumpedLastTick) {
      var turn = String.format(this.stringFormat, state.yawTurn());
      this.textLine.updateAndFlush(turn);
    }

    this.jumpedLastTick = state.isJumpTick();
  }

  @Getter
  public static class SecondTurnWidgetConfig extends TextHudWidgetConfig {

    @SliderSetting(min = 0, max = 10)
    private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
  }
}
