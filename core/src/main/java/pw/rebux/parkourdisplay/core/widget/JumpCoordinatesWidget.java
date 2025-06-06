package pw.rebux.parkourdisplay.core.widget;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.config.JumpCoordinatesWidgetConfig;

public class JumpCoordinatesWidget extends TextHudWidget<JumpCoordinatesWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private final TextLine[] textLines = new TextLine[3];

  public JumpCoordinatesWidget(ParkourDisplayAddon addon) {
    super("jump_coordinates", JumpCoordinatesWidgetConfig.class);

    this.bindCategory(addon.category());

    this.addon = addon;
  }

  @Override
  public void load(JumpCoordinatesWidgetConfig config) {
    super.load(config);

    if (config.singleLine().get()) {
      textLines[0] = createLine("Jump XYZ", "");
    } else {
      textLines[0] = createLine("Jump X", "");
      textLines[1] = createLine("Jump Y", "");
      textLines[2] = createLine("Jump Z", "");
    }
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var singleLine = this.config.singleLine().get();
    var decimalPlaces = this.config.decimalPlaces().get();
    var stringFormat = "%%.%df".formatted(decimalPlaces);

    var x = String.format(stringFormat, this.addon.playerParkourState().jumpX());
    var y = String.format(stringFormat, this.addon.playerParkourState().jumpY());
    var z = String.format(stringFormat, this.addon.playerParkourState().jumpZ());

    if (singleLine) {
      this.textLines[0].updateAndFlush("%s %s %s".formatted(x, y, z));
    } else {
      this.textLines[0].updateAndFlush(x);
      this.textLines[1].updateAndFlush(y);
      this.textLines[2].updateAndFlush(z);
    }
  }
}
