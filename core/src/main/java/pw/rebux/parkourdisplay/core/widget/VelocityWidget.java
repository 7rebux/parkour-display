package pw.rebux.parkourdisplay.core.widget;

import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.config.VelocityWidgetConfig;

public class VelocityWidget extends TextHudWidget<VelocityWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private final TextLine[] textLines = new TextLine[3];

  public VelocityWidget(ParkourDisplayAddon addon) {
    super("velocity", VelocityWidgetConfig.class);

    this.bindCategory(addon.category());

    this.addon = addon;
  }

  @Override
  public void load(VelocityWidgetConfig config) {
    super.load(config);

    if (config.singleLine().get()) {
      textLines[0] = createLine(Component.translatable("parkourdisplay.labels.velocity.single"), "");
    } else {
      textLines[0] = createLine(Component.translatable("parkourdisplay.labels.velocity.x"), "");
      textLines[1] = createLine(Component.translatable("parkourdisplay.labels.velocity.y"), "");
      textLines[2] = createLine(Component.translatable("parkourdisplay.labels.velocity.z"), "");
    }
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var singleLine = this.config.singleLine().get();
    var decimalPlaces = this.config.decimalPlaces().get();
    var stringFormat = "%%.%df".formatted(decimalPlaces);

    var x = String.format(stringFormat, this.addon.playerParkourState().velocityX());
    var y = String.format(stringFormat, this.addon.playerParkourState().velocityY());
    var z = String.format(stringFormat, this.addon.playerParkourState().velocityZ());

    if (singleLine) {
      this.textLines[0].updateAndFlush("%s %s %s".formatted(x, y, z));
    } else {
      this.textLines[0].updateAndFlush(x);
      this.textLines[1].updateAndFlush(y);
      this.textLines[2].updateAndFlush(z);
    }
  }
}
