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
    this.textLine.updateAndFlush(this.buildInputString());
  }

  private String buildInputString() {
    var inputUtil = this.addon.minecraftInputUtil();
    var input = new StringBuilder();
    var hasMovement = false;

    if (inputUtil.forwardKey().isDown()) { input.append("W"); hasMovement = true; }
    if (inputUtil.leftKey().isDown())    { input.append("A"); hasMovement = true; }
    if (inputUtil.backKey().isDown())    { input.append("S"); hasMovement = true; }
    if (inputUtil.rightKey().isDown())   { input.append("D"); hasMovement = true; }

    if (inputUtil.jumpKey().isDown()) {
      if (hasMovement) input.append(" ");
      input.append("Jump");
    }

    if (inputUtil.sneakKey().isDown()) {
      if (hasMovement || !input.isEmpty()) input.append(" ");
      input.append("Sneak");
    }

    if (input.isEmpty()) {
      input.append("-");
    }

    return input.toString();
  }
}
