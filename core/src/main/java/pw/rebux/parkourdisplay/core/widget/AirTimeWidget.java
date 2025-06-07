package pw.rebux.parkourdisplay.core.widget;

import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class AirTimeWidget extends TextHudWidget<TextHudWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;

  public AirTimeWidget(ParkourDisplayAddon addon) {
    super("air_time");

    this.bindCategory(addon.category());

    this.addon = addon;
  }

  @Override
  public void load(TextHudWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(Component.translatable("parkourdisplay.labels.air_time"), "");
  }

  @Override
  public void onTick(boolean isEditorContext) {
    this.textLine.updateAndFlush(this.addon.playerParkourState().lastDuration());
  }
}
