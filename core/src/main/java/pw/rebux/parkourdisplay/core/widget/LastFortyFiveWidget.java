package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.LastFortyFiveWidget.LastFortyFiveWidgetConfig;

/// The yaw delta at the tick strafe input is added while airborne.
///
/// References: [45 Strafe - MCPK Wiki](https://www.mcpk.wiki/wiki/45_Strafe)
public class LastFortyFiveWidget extends TextHudWidget<LastFortyFiveWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;
  private String stringFormat;

  private boolean lastTickMovingForward;
  private boolean lastTickMovingSideways;

  public LastFortyFiveWidget(ParkourDisplayAddon addon) {
    super("last_forty_five", LastFortyFiveWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(LastFortyFiveWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(translatable("parkourdisplay.labels.last_forty_five"), 0);
    this.stringFormat = "%%.%df".formatted(config.decimalPlaces().get());
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var state = this.addon.playerState();

    if (player == null) {
      return;
    }

    var movingForward = player.getForwardMovingSpeed() != 0;
    var movingSideways = checkMovingSideways(player);

    if (this.lastTickMovingForward && movingForward
        && !this.lastTickMovingSideways && movingSideways
        && !state.currentTick().onGround()
        && (!this.config.strict.get() || state.lastTick().onGround())
    ) {
      var lastFF = String.format(this.stringFormat, state.yawTurn());
      this.textLine.updateAndFlush(lastFF);
    }

    this.lastTickMovingForward = movingForward;
    this.lastTickMovingSideways = movingSideways;
  }

  private boolean checkMovingSideways(ClientPlayer player) {
    try {
      return player.getStrafeMovingSpeed() != 0;
    } catch (Throwable throwable) {
      // Somehow ClientPlayer#getStrafeMovingSpeed is not implemented in newer versions.
      // We use a fallback here to check keyboard input instead.
      return this.addon.minecraftInputUtil().isMovingSideways();
    }
  }

  @Getter
  public static class LastFortyFiveWidgetConfig extends TextHudWidgetConfig {

    /// Only updates the label if strafe was added exactly 1t after jumping.
    @SwitchSetting
    private final ConfigProperty<Boolean> strict = new ConfigProperty<>(false);

    @SliderSetting(min = 0, max = 10)
    private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
  }
}
