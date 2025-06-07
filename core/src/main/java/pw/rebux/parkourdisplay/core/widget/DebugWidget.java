package pw.rebux.parkourdisplay.core.widget;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class DebugWidget extends TextHudWidget<TextHudWidgetConfig> {

  private final ParkourDisplayAddon addon;

  public DebugWidget(ParkourDisplayAddon addon) {
    super("debug");

    this.addon = addon;
  }

  @Override
  public void load(TextHudWidgetConfig config) {
    super.load(config);


  }

  @Override
  public void onTick(boolean isEditorContext) {

  }
}
