package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.LastTurnWidget.LastTurnWidgetConfig;

public class LastTurnWidget extends TextHudWidget<LastTurnWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;
  private String stringFormat;

  public LastTurnWidget(ParkourDisplayAddon addon) {
    super("last_turn", LastTurnWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(LastTurnWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(translatable("parkourdisplay.labels.last_turn"), 0);
    this.stringFormat = "%%.%df".formatted(config.decimalPlaces().get());
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var lastTurn = String.format(this.stringFormat, this.addon.playerParkourState().lastTurn());

    this.textLine.updateAndFlush(lastTurn);
  }

  @Getter
  @Accessors(fluent = true)
  public static class LastTurnWidgetConfig extends TextHudWidgetConfig {

    @SliderSetting(min = 0, max = 10)
    private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
  }
}
