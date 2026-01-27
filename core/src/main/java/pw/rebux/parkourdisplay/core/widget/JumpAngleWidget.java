package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.MathHelper;
import pw.rebux.parkourdisplay.core.widget.JumpAngleWidget.JumpAngleWidgetConfig;

public class JumpAngleWidget extends TextHudWidget<JumpAngleWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;
  private String stringFormat;

  public JumpAngleWidget(ParkourDisplayAddon addon) {
    super("jump_angle", JumpAngleWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(JumpAngleWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(translatable("parkourdisplay.labels.jump_angle"), 0);
    this.stringFormat = "%%.%df".formatted(config.decimalPlaces().get());
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var state = this.addon.playerState();

    // Make sure the player initiated a jump
    if (!state.lastTick().onGround() || state.currentTick().onGround()) {
      return;
    }

    var yaw = String.format(this.stringFormat, MathHelper.formatYaw(state.currentTick().yaw()));
    this.textLine.updateAndFlush(yaw);
  }

  @Getter
  public static class JumpAngleWidgetConfig extends TextHudWidgetConfig {

    @SliderSetting(min = 0, max = 10)
    private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
  }
}
