package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.LastFortyFiveWidget.LastFortyFiveWidgetConfig;

/// [45 Strafe - MCPK Wiki](https://www.mcpk.wiki/wiki/45_Strafe)
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
    var movingSideways = tryGetMovingSideways(player);

    // TODO: This could also check if in the last tick a jump was initiated.
    // Player attempted 45 degree strafe
    if (this.lastTickMovingForward
        && !this.lastTickMovingSideways
        && movingForward
        && movingSideways
        && !state.currentTick().onGround()
    ) {
      var lastFF = String.format(this.stringFormat, state.yawTurn());
      this.textLine.updateAndFlush(lastFF);
    }

    this.lastTickMovingForward = movingForward;
    this.lastTickMovingSideways = movingSideways;
  }

  // TODO: This is somehow still broken in version 1.21.4
  private boolean tryGetMovingSideways(ClientPlayer player) {
    try {
      return player.getStrafeMovingSpeed() != 0;
    } catch (Throwable throwable) {
      return false;
    }
  }

  @Getter
  public static class LastFortyFiveWidgetConfig extends TextHudWidgetConfig {

    @SliderSetting(min = 0, max = 10)
    private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
  }
}
