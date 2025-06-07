package pw.rebux.parkourdisplay.core.widget;

import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.config.LandingCoordinatesWidgetConfig;

public class LandingCoordinatesWidget extends TextHudWidget<LandingCoordinatesWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private final TextLine[] textLines = new TextLine[3];

  public LandingCoordinatesWidget(ParkourDisplayAddon addon) {
    super("landing_coordinates", LandingCoordinatesWidgetConfig.class);

    this.bindCategory(addon.category());

    this.addon = addon;
  }

  @Override
  public void load(LandingCoordinatesWidgetConfig config) {
    super.load(config);

    if (config.singleLine().get()) {
      textLines[0] = createLine(Component.translatable("parkourdisplay.labels.landing_coordinates.single"), "");
    } else {
      textLines[0] = createLine(Component.translatable("parkourdisplay.labels.landing_coordinates.x"), "");
      textLines[1] = createLine(Component.translatable("parkourdisplay.labels.landing_coordinates.y"), "");
      textLines[2] = createLine(Component.translatable("parkourdisplay.labels.landing_coordinates.z"), "");
    }
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var singleLine = this.config.singleLine().get();
    var decimalPlaces = this.config.decimalPlaces().get();
    var stringFormat = "%%.%df".formatted(decimalPlaces);

    var x = String.format(stringFormat, this.addon.playerParkourState().landingX());
    var y = String.format(stringFormat, this.addon.playerParkourState().landingY());
    var z = String.format(stringFormat, this.addon.playerParkourState().landingZ());

    if (singleLine) {
      this.textLines[0].updateAndFlush("%s %s %s".formatted(x, y, z));
    } else {
      this.textLines[0].updateAndFlush(x);
      this.textLines[1].updateAndFlush(y);
      this.textLines[2].updateAndFlush(z);
    }
  }
}
