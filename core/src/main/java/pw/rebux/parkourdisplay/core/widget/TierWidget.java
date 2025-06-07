package pw.rebux.parkourdisplay.core.widget;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class TierWidget extends TextHudWidget<TextHudWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;

  public TierWidget(ParkourDisplayAddon addon) {
    super("tier");

    this.bindCategory(addon.category());

    this.addon = addon;
  }

  @Override
  public void load(TextHudWidgetConfig config) {
    super.load(config);
    this.textLine = createLine("Tier", "");
  }

  @Override
  public void onTick(boolean isEditorContext) {
    // https://www.mcpk.wiki/wiki/Tiers
    var tier = 12 - this.addon.playerParkourState().lastDuration();

    this.textLine.updateAndFlush(tier);
  }
}
