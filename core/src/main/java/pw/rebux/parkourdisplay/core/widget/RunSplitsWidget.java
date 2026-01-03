package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.state.RunSplit;

public final class RunSplitsWidget extends TextHudWidget<TextHudWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;

  public RunSplitsWidget(ParkourDisplayAddon addon) {
    super("run_splits");
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(TextHudWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(translatable("parkourdisplay.labels.run_splits"), "");
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var splits = this.addon.playerParkourState().runSplits().stream()
        .filter(RunSplit::passed)
        .map(RunSplit::lastTicks)
        .toList();

    this.textLine.updateAndFlush(splits);
  }
}
