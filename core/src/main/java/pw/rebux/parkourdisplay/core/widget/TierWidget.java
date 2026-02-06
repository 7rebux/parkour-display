package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

/// [Tiers - MCPK Wiki](https://www.mcpk.wiki/wiki/Tiers)
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

    this.textLine = createLine(translatable("parkourdisplay.labels.tier"), "");
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var state = this.addon.playerState();

    if (state.airTime() > 0) {
      this.textLine.updateAndFlush(12 - state.airTime());
    }
  }
}
