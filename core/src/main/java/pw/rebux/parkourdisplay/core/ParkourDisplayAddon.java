package pw.rebux.parkourdisplay.core;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;
import pw.rebux.parkourdisplay.core.listener.GameTickListener;
import pw.rebux.parkourdisplay.core.state.PlayerParkourState;
import pw.rebux.parkourdisplay.core.widget.*;

@AddonMain
@Accessors(fluent = true)
public class ParkourDisplayAddon extends LabyAddon<ParkourDisplayConfiguration> {

  @Getter
  private final PlayerParkourState playerParkourState = new PlayerParkourState();

  @Override
  protected void enable() {
    this.registerSettingCategory();

    this.registerListener(new GameTickListener(this));

    this.labyAPI().hudWidgetRegistry().register(new AirTimeWidget(this));
    this.labyAPI().hudWidgetRegistry().register(new JumpCoordinatesWidget(this));
    this.labyAPI().hudWidgetRegistry().register(new JumpRotationWidget(this));
    this.labyAPI().hudWidgetRegistry().register(new LastTimingWidget(this));
  }

  @Override
  protected Class<ParkourDisplayConfiguration> configurationClass() {
    return ParkourDisplayConfiguration.class;
  }
}
