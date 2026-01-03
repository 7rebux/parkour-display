package pw.rebux.parkourdisplay.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.models.addon.annotation.AddonMain;
import pw.rebux.parkourdisplay.core.command.BaseCommand;
import pw.rebux.parkourdisplay.core.landingblock.LandingBlockManager;
import pw.rebux.parkourdisplay.core.listener.GameTickListener;
import pw.rebux.parkourdisplay.core.listener.RenderWorldListener;
import pw.rebux.parkourdisplay.core.macro.MacroManager;
import pw.rebux.parkourdisplay.core.splits.SplitsManager;
import pw.rebux.parkourdisplay.core.state.PlayerParkourState;
import pw.rebux.parkourdisplay.core.state.TickInput;
import pw.rebux.parkourdisplay.core.util.adapter.TickInputAdapter;
import pw.rebux.parkourdisplay.core.widget.AirTimeWidget;
import pw.rebux.parkourdisplay.core.widget.GroundTimeWidget;
import pw.rebux.parkourdisplay.core.widget.HitAngleWidget;
import pw.rebux.parkourdisplay.core.widget.HitCoordinatesWidget;
import pw.rebux.parkourdisplay.core.widget.HitVelocityWidget;
import pw.rebux.parkourdisplay.core.widget.JumpAngleWidget;
import pw.rebux.parkourdisplay.core.widget.JumpCoordinatesWidget;
import pw.rebux.parkourdisplay.core.widget.LandingCoordinatesWidget;
import pw.rebux.parkourdisplay.core.widget.LastFortyFiveWidget;
import pw.rebux.parkourdisplay.core.widget.LastInputWidget;
import pw.rebux.parkourdisplay.core.widget.LastLandingBlockOffsetsWidget;
import pw.rebux.parkourdisplay.core.widget.LastTimingWidget;
import pw.rebux.parkourdisplay.core.widget.LastTurnWidget;
import pw.rebux.parkourdisplay.core.widget.RunGroundTimeWidget;
import pw.rebux.parkourdisplay.core.widget.RunSplitsWidget;
import pw.rebux.parkourdisplay.core.widget.SpeedVectorWidget;
import pw.rebux.parkourdisplay.core.widget.TierWidget;
import pw.rebux.parkourdisplay.core.widget.VelocityWidget;

@AddonMain
@Accessors(fluent = true)
public class ParkourDisplayAddon extends LabyAddon<ParkourDisplayConfiguration> {

  public static final String NAMESPACE = "parkourdisplay";
  public static final File DATA_DIR = new File("parkour-display");

  @Getter
  private final Gson gson = new GsonBuilder()
      .registerTypeAdapter(TickInput.class, new TickInputAdapter())
      .setPrettyPrinting()
      .create();

  @Getter
  private final HudWidgetCategory category = new HudWidgetCategory(this, NAMESPACE);

  @Getter
  private final LandingBlockManager landingBlockManager = new LandingBlockManager(this);

  @Getter
  private final MacroManager macroManager = new MacroManager(this);

  @Getter
  private final SplitsManager splitsManager = new SplitsManager(this);

  @Getter
  private final PlayerParkourState playerParkourState = new PlayerParkourState();

  @Override
  protected void enable() {
    var hudWidgetRegistry = this.labyAPI().hudWidgetRegistry();

    this.registerSettingCategory();

    this.registerListener(new GameTickListener(this));
    this.registerListener(new RenderWorldListener(this));

    this.registerCommand(new BaseCommand(this));

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
    hudWidgetRegistry.register(new LastTimingWidget(this));
    hudWidgetRegistry.register(new LastTurnWidget(this));
    hudWidgetRegistry.register(new LastFortyFiveWidget(this));
    hudWidgetRegistry.register(new LastInputWidget(this));
    hudWidgetRegistry.register(new LastLandingBlockOffsetsWidget(this));
    hudWidgetRegistry.register(new RunGroundTimeWidget(this));
    hudWidgetRegistry.register(new RunSplitsWidget(this));

    if (!DATA_DIR.exists() && !DATA_DIR.mkdir()) {
      throw new RuntimeException("Failed to create data directory: " + DATA_DIR.getAbsolutePath());
    }
  }

  @Override
  protected Class<ParkourDisplayConfiguration> configurationClass() {
    return ParkourDisplayConfiguration.class;
  }
}
