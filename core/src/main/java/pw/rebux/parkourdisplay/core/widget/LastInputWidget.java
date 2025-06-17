package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class LastInputWidget extends TextHudWidget<TextHudWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;

  public LastInputWidget(ParkourDisplayAddon addon) {
    super("last_input");
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(TextHudWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(translatable("parkourdisplay.labels.last_input"), "-");
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var lastInput = this.addon.playerParkourState().lastInput();

    this.textLine.updateAndFlush(lastInput);
  }
}
