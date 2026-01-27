package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.GroundTimeWidget.GroundTimeWidgetConfig;

public class GroundTimeWidget extends TextHudWidget<GroundTimeWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;

  public GroundTimeWidget(ParkourDisplayAddon addon) {
    super("ground_time", GroundTimeWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(GroundTimeWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(translatable("parkourdisplay.labels.ground_time"), 0);
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var state = this.addon.playerState();
    var initiatedJump = state.lastTick().onGround() && !state.currentTick().onGround();

    if (this.config.incremental().get() || initiatedJump) {
      this.textLine.updateAndFlush(state.groundTime());
    }
  }

  @Getter
  public static class GroundTimeWidgetConfig extends TextHudWidgetConfig {

    /**
     * Whether to update the label continuously while on ground.
     */
    @SwitchSetting
    private final ConfigProperty<Boolean> incremental = new ConfigProperty<>(false);
  }
}
