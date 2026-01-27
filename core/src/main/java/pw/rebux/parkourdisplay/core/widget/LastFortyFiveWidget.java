package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.LastFortyFiveWidget.LastFortyFiveWidgetConfig;

public class LastFortyFiveWidget extends TextHudWidget<LastFortyFiveWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;
  private String stringFormat;

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
    var state = this.addon.playerState();

    // Player attempted 45 degree strafe
    if (state.lastTick().movingForward()
        && !state.lastTick().movingSideways()
        && state.currentTick().movingForward()
        && state.currentTick().movingSideways()
        && !state.currentTick().onGround()
    ) {
      var lastFF = String.format(
          this.stringFormat,
          state.currentTick().yaw() - state.lastTick().yaw()
      );

      this.textLine.updateAndFlush(lastFF);
    }
  }

  @Getter
  public static class LastFortyFiveWidgetConfig extends TextHudWidgetConfig {

    @SliderSetting(min = 0, max = 10)
    private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
  }
}
