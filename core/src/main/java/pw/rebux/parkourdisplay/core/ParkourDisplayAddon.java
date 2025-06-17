package pw.rebux.parkourdisplay.core;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.models.addon.annotation.AddonMain;
import pw.rebux.parkourdisplay.core.listener.GameTickListener;
import pw.rebux.parkourdisplay.core.state.PlayerParkourState;
import pw.rebux.parkourdisplay.core.widget.AirTimeWidget;
import pw.rebux.parkourdisplay.core.widget.GroundTimeWidget;
import pw.rebux.parkourdisplay.core.widget.HitAngleWidget;
import pw.rebux.parkourdisplay.core.widget.HitCoordinatesWidget;
import pw.rebux.parkourdisplay.core.widget.HitVelocityWidget;
import pw.rebux.parkourdisplay.core.widget.JumpAngleWidget;
import pw.rebux.parkourdisplay.core.widget.JumpCoordinatesWidget;
import pw.rebux.parkourdisplay.core.widget.LandingCoordinatesWidget;
import pw.rebux.parkourdisplay.core.widget.LastInputWidget;
import pw.rebux.parkourdisplay.core.widget.LastTurnWidget;
import pw.rebux.parkourdisplay.core.widget.SpeedVectorWidget;
import pw.rebux.parkourdisplay.core.widget.TierWidget;
import pw.rebux.parkourdisplay.core.widget.VelocityWidget;

@AddonMain
@Accessors(fluent = true)
public class ParkourDisplayAddon extends LabyAddon<ParkourDisplayConfiguration> {

  @Getter
  private final HudWidgetCategory category = new HudWidgetCategory(this, "parkourdisplay");

  @Getter
  private final PlayerParkourState playerParkourState = new PlayerParkourState();

  @Override
  protected void enable() {
    var hudWidgetRegistry = this.labyAPI().hudWidgetRegistry();

    this.registerSettingCategory();

    this.registerListener(new GameTickListener(this));

    hudWidgetRegistry.categoryRegistry().register(this.category);
    hudWidgetRegistry.register(new VelocityWidget(this));
    hudWidgetRegistry.register(new SpeedVectorWidget(this));
    hudWidgetRegistry.register(new GroundTimeWidget(this));
    hudWidgetRegistry.register(new AirTimeWidget(this));
    hudWidgetRegistry.register(new TierWidget(this));
    hudWidgetRegistry.register(new JumpCoordinatesWidget(this));
    hudWidgetRegistry.register(new JumpAngleWidget(this));
    hudWidgetRegistry.register(new LandingCoordinatesWidget(this));
    hudWidgetRegistry.register(new HitCoordinatesWidget(this));
    hudWidgetRegistry.register(new HitAngleWidget(this));
    hudWidgetRegistry.register(new HitVelocityWidget(this));
    // TODO: Not implemented yet
    // hudWidgetRegistry.register(new LastTimingWidget(this));
    hudWidgetRegistry.register(new LastTurnWidget(this));
    // TODO: API is broken in 1.21.4
    // hudWidgetRegistry.register(new LastFortyFiveWidget(this));
    hudWidgetRegistry.register(new LastInputWidget(this));
  }

  @Override
  protected Class<ParkourDisplayConfiguration> configurationClass() {
    return ParkourDisplayConfiguration.class;
  }
}
