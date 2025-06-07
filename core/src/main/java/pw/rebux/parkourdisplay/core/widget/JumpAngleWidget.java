package pw.rebux.parkourdisplay.core.widget;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.MathsUtil;
import pw.rebux.parkourdisplay.core.widget.config.JumpAngleWidgetConfig;

public class JumpAngleWidget extends TextHudWidget<JumpAngleWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;

  public JumpAngleWidget(ParkourDisplayAddon addon) {
    super("jump_angle", JumpAngleWidgetConfig.class);

    this.bindCategory(addon.category());

    this.addon = addon;
  }

  @Override
  public void load(JumpAngleWidgetConfig config) {
    super.load(config);

    textLine = createLine("Jump Angle", "");
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var decimalPlaces = this.config.decimalPlaces().get();
    var stringFormat = "%%.%df".formatted(decimalPlaces);

    var facing = String.format(
        stringFormat,
        MathsUtil.formatYaw(this.addon.playerParkourState().jumpYaw()));

    textLine.updateAndFlush(facing);
  }
}
