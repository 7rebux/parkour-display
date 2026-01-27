package pw.rebux.parkourdisplay.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import lombok.Getter;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.gui.hud.binding.category.HudWidgetCategory;
import net.labymod.api.models.addon.annotation.AddonMain;
import pw.rebux.parkourdisplay.core.command.BaseCommand;
import pw.rebux.parkourdisplay.core.landingblock.LandingBlockListener;
import pw.rebux.parkourdisplay.core.landingblock.LandingBlockManager;
import pw.rebux.parkourdisplay.core.listener.GameTickListener;
import pw.rebux.parkourdisplay.core.macro.MacroManager;
import pw.rebux.parkourdisplay.core.macro.MacroRunner;
import pw.rebux.parkourdisplay.core.run.RunListener;
import pw.rebux.parkourdisplay.core.run.RunState;
import pw.rebux.parkourdisplay.core.run.split.SplitManager;
import pw.rebux.parkourdisplay.core.state.PlayerState;
import pw.rebux.parkourdisplay.core.state.PlayerStateListener;
import pw.rebux.parkourdisplay.core.state.TickInput;
import pw.rebux.parkourdisplay.core.util.MinecraftInputUtil;
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
@Getter
public final class ParkourDisplayAddon extends LabyAddon<ParkourDisplayConfiguration> {

  public static final String NAMESPACE = "parkourdisplay";
  public static final File DATA_DIR = new File("parkour-display");

  private final Gson gson = new GsonBuilder()
      .setPrettyPrinting()
      .registerTypeAdapter(TickInput.class, new TickInputAdapter())
      .create();
  private final HudWidgetCategory category = new HudWidgetCategory(this, NAMESPACE);
  private final LandingBlockManager landingBlockManager = new LandingBlockManager(this);
  private final MacroManager macroManager = new MacroManager(this);
  private final SplitManager splitManager = new SplitManager(this);
  private final PlayerState playerState = new PlayerState();
  private final RunState runState = new RunState();

  private MinecraftInputUtil minecraftInputUtil;

  @Override
  protected void enable() {
    this.minecraftInputUtil = new MinecraftInputUtil(this);

    var hudWidgetRegistry = this.labyAPI().hudWidgetRegistry();

    this.registerSettingCategory();

    this.registerListener(new GameTickListener(this));
    this.registerListener(new MacroRunner(this));
    this.registerListener(new RunListener(this));
    this.registerListener(new LandingBlockListener(this));
    this.registerListener(new PlayerStateListener(this));

    this.registerCommand(new BaseCommand(this));

    hudWidgetRegistry.categoryRegistry().register(this.category);
    hudWidgetRegistry.register(new AirTimeWidget(this));
    hudWidgetRegistry.register(new GroundTimeWidget(this));
    hudWidgetRegistry.register(new TierWidget(this));
    hudWidgetRegistry.register(new VelocityWidget(this));
    hudWidgetRegistry.register(new SpeedVectorWidget(this));
    hudWidgetRegistry.register(new JumpCoordinatesWidget(this));
    hudWidgetRegistry.register(new JumpAngleWidget(this));
    hudWidgetRegistry.register(new LandingCoordinatesWidget(this));
    hudWidgetRegistry.register(new HitCoordinatesWidget(this));
    hudWidgetRegistry.register(new HitAngleWidget(this));
    hudWidgetRegistry.register(new HitVelocityWidget(this));
    hudWidgetRegistry.register(new LastTurnWidget(this));
    hudWidgetRegistry.register(new LastFortyFiveWidget(this));
    hudWidgetRegistry.register(new LastInputWidget(this));
    hudWidgetRegistry.register(new LastTimingWidget(this));
    hudWidgetRegistry.register(new LastLandingBlockOffsetsWidget(this));
    hudWidgetRegistry.register(new RunGroundTimeWidget(this));
    hudWidgetRegistry.register(new RunSplitsWidget(this));
  }

  @Override
  protected Class<ParkourDisplayConfiguration> configurationClass() {
    return ParkourDisplayConfiguration.class;
  }
}
