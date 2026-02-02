package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class RunGroundTimeWidget extends TextHudWidget<TextHudWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;

  public RunGroundTimeWidget(ParkourDisplayAddon addon) {
    super("run_ground_time");
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(TextHudWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(translatable("parkourdisplay.labels.run_ground_time"), "");
  }

  @Override
  public void onTick(boolean isEditorContext) {
    this.textLine.updateAndFlush(this.addon.runState().runGroundTime());
  }

  @Override
  public boolean isVisibleInGame() {
    var run = this.addon.runState();
    return run.startPosition() != null && run.endSplit() != null;
  }
}
