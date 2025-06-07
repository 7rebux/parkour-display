package pw.rebux.parkourdisplay.core.widget;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class GroundTimeWidget extends TextHudWidget<TextHudWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;

  public GroundTimeWidget(ParkourDisplayAddon addon) {
    super("ground_time");

    this.bindCategory(addon.category());

    this.addon = addon;
  }

  @Override
  public void load(TextHudWidgetConfig config) {
    super.load(config);

    this.textLine = createLine("Ground Time", "");
  }

  @Override
  public void onTick(boolean isEditorContext) {
    this.textLine.updateAndFlush(addon.playerParkourState().lastGroundDuration());
  }
}
