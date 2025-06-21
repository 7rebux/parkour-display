package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class LastTimingWidget extends TextHudWidget<TextHudWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;

  public LastTimingWidget(ParkourDisplayAddon addon) {
    super("last_timing");
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(TextHudWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(translatable("parkourdisplay.labels.last_timing"), "-");
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var lastTiming = this.addon.playerParkourState().lastTiming();

    this.textLine.updateAndFlush(lastTiming);
  }
}
