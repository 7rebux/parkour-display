package pw.rebux.parkourdisplay.core.widget;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.config.JumpRotationWidgetConfig;

public class JumpRotationWidget extends TextHudWidget<JumpRotationWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private final TextLine[] textLines = new TextLine[2];

  public JumpRotationWidget(ParkourDisplayAddon addon) {
    super("jump_rotation", JumpRotationWidgetConfig.class);
    this.addon = addon;
  }

  @Override
  public void load(JumpRotationWidgetConfig config) {
    super.load(config);

    if (config.singleLine().get()) {
      textLines[0] = createLine("Jump Rotation", "");
    } else {
      textLines[0] = createLine("Jump Yaw", "");
      textLines[1] = createLine("Jump Pitch", "");
    }
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var singleLine = this.config.singleLine().get();
    var decimalPlaces = this.config.decimalPlaces().get();
    var stringFormat = "%%.%df".formatted(decimalPlaces);

    var yaw = String.format(stringFormat, this.addon.playerParkourState().jumpYaw());
    var pitch = String.format(stringFormat, this.addon.playerParkourState().jumpPitch());

    if (singleLine) {
      this.textLines[0].updateAndFlush("%s, %s".formatted(yaw, pitch));
    } else {
      this.textLines[0].updateAndFlush(yaw);
      this.textLines[1].updateAndFlush(pitch);
    }
  }
}
