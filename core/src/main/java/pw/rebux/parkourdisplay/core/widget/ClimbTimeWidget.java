package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.ClimbTimeWidget.ClimbTimeWidgetConfig;

public class ClimbTimeWidget extends TextHudWidget<ClimbTimeWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;

  public ClimbTimeWidget(ParkourDisplayAddon addon) {
    super("climb_time", ClimbTimeWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(ClimbTimeWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(translatable("parkourdisplay.labels.climb_time"), 0);
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var state = this.addon.playerState();
    var shouldUpdate = this.config.incremental().get() || state.currentTick().onGround();

    if (state.climbTime() > 0 && shouldUpdate) {
      this.textLine.updateAndFlush(state.climbTime());
    }
  }

  @Getter
  public static class ClimbTimeWidgetConfig extends TextHudWidgetConfig {

    /**
     * Whether to update the label continuously while climbing.
     */
    @SwitchSetting
    private final ConfigProperty<Boolean> incremental = new ConfigProperty<>(true);
  }
}
