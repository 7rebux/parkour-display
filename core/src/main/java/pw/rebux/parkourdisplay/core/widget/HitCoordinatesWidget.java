package pw.rebux.parkourdisplay.core.widget;

import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.config.HitCoordinatesWidgetConfig;

public class HitCoordinatesWidget extends TextHudWidget<HitCoordinatesWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private final TextLine[] textLines = new TextLine[3];

  public HitCoordinatesWidget(ParkourDisplayAddon addon) {
    super("hit_coordinates", HitCoordinatesWidgetConfig.class);

    this.bindCategory(addon.category());

    this.addon = addon;
  }

  @Override
  public void load(HitCoordinatesWidgetConfig config) {
    super.load(config);

    if (config.singleLine().get()) {
      textLines[0] = createLine(Component.translatable("parkourdisplay.labels.hit_coordinates.single"), "");
    } else {
      textLines[0] = createLine(Component.translatable("parkourdisplay.labels.hit_coordinates.x"), "");
      textLines[1] = createLine(Component.translatable("parkourdisplay.labels.hit_coordinates.y"), "");
      textLines[2] = createLine(Component.translatable("parkourdisplay.labels.hit_coordinates.z"), "");
    }
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var singleLine = this.config.singleLine().get();
    var decimalPlaces = this.config.decimalPlaces().get();
    var stringFormat = "%%.%df".formatted(decimalPlaces);

    var x = String.format(stringFormat, this.addon.playerParkourState().hitX());
    var y = String.format(stringFormat, this.addon.playerParkourState().hitY());
    var z = String.format(stringFormat, this.addon.playerParkourState().hitZ());

    if (singleLine) {
      this.textLines[0].updateAndFlush("%s %s %s".formatted(x, y, z));
    } else {
      this.textLines[0].updateAndFlush(x);
      this.textLines[1].updateAndFlush(y);
      this.textLines[2].updateAndFlush(z);
    }
  }
}
