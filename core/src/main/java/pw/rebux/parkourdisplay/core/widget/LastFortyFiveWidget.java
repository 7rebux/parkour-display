package pw.rebux.parkourdisplay.core.widget;

import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.config.LastFortyFiveWidgetConfig;

public class LastFortyFiveWidget extends TextHudWidget<LastFortyFiveWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;

  public LastFortyFiveWidget(ParkourDisplayAddon addon) {
    super("last_forty_five", LastFortyFiveWidgetConfig.class);

    this.bindCategory(addon.category());

    this.addon = addon;
  }

  @Override
  public void load(LastFortyFiveWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(Component.translatable("parkourdisplay.labels.last_forty_five"), "");
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var decimalPlaces = this.config.decimalPlaces().get();
    var stringFormat = "%%.%df".formatted(decimalPlaces);
    var lastFF = String.format(stringFormat, addon.playerParkourState().lastFF());

    this.textLine.updateAndFlush(lastFF);
  }
}
