package pw.rebux.parkourdisplay.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import pw.rebux.parkourdisplay.common.engine.MacroEngine;
import pw.rebux.parkourdisplay.common.engine.PlayerStateEngine;
import pw.rebux.parkourdisplay.common.engine.RunEngine;
import pw.rebux.parkourdisplay.common.macro.MacroRunner;
import pw.rebux.parkourdisplay.common.macro.MacroTickState;
import pw.rebux.parkourdisplay.common.persist.MacroRepository;
import pw.rebux.parkourdisplay.common.persist.MacroTickStateTypeAdapter;
import pw.rebux.parkourdisplay.common.run.RunState;
import pw.rebux.parkourdisplay.common.state.PlayerState;
import pw.rebux.parkourdisplay.fabric.command.ParkourCommands;
import pw.rebux.parkourdisplay.fabric.platform.FabricChatOutput;
import pw.rebux.parkourdisplay.fabric.platform.FabricContext;
import pw.rebux.parkourdisplay.fabric.platform.FabricInputController;
import pw.rebux.parkourdisplay.fabric.platform.FabricRunConfig;
import pw.rebux.parkourdisplay.fabric.platform.FabricRunRepository;

/// Entry point for the standalone Fabric build of ParkourDisplay. Wires the shared run-splits and
/// macro engines to Fabric/Yarn APIs. The engines are created lazily on the first client tick, once
/// the client options they depend on are available.
public final class ParkourDisplayFabric implements ClientModInitializer {

  private Gson gson;
  private Path dataDir;
  private FabricRunConfig config;
  private MacroRepository macroRepository;
  private FabricRunRepository runRepository;

  private boolean initialized = false;
  private FabricContext context;
  private PlayerState playerState;
  private RunState runState;
  private MacroRunner macroRunner;
  private PlayerStateEngine playerStateEngine;
  private RunEngine runEngine;
  private MacroEngine macroEngine;

  @Override
  public void onInitializeClient() {
    this.gson = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(MacroTickState.class, new MacroTickStateTypeAdapter())
        .create();

    var loader = FabricLoader.getInstance();
    this.dataDir = loader.getGameDir().resolve("parkour-display");
    this.config = new FabricRunConfig(loader.getConfigDir(), this.gson);
    this.macroRepository = new MacroRepository(this.dataDir, this.gson);
    this.runRepository = new FabricRunRepository(this.dataDir, this.gson);

    // Macros step before the tick's physics run, mirroring LabyMod's pre-tick phase.
    ClientTickEvents.START_CLIENT_TICK.register(client -> {
      this.ensureInitialized(client);
      this.macroEngine.onTick(false);
    });

    // Player state and run tracking read the final position after the tick, matching the
    // ordering the run logic depends on: begin -> run -> macro(post) -> end.
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      this.ensureInitialized(client);
      var player = this.context.player();
      if (player != null) {
        this.playerStateEngine.beginTick(player);
        this.runEngine.onTick();
        this.macroEngine.onTick(true);
        this.playerStateEngine.endTick(player);
      } else {
        this.macroEngine.onTick(true);
      }
    });

    // Sub-tick rotation interpolation for smooth macro turning.
    WorldRenderEvents.START.register(renderContext -> {
      if (this.initialized) {
        this.macroEngine.onRenderFrame(this.context.partialTicks());
      }
    });

    ClientCommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess) -> ParkourCommands.register(dispatcher, this));
  }

  private void ensureInitialized(MinecraftClient client) {
    if (this.initialized) {
      return;
    }

    this.playerState = new PlayerState();
    var input = new FabricInputController(client.options);
    var chat = new FabricChatOutput(client);
    this.context = new FabricContext(client, input, chat, this.config, this.dataDir);
    this.runState = new RunState(this.context, this.playerState);
    this.macroRunner = new MacroRunner(this.context);
    this.playerStateEngine = new PlayerStateEngine(this.playerState);
    this.runEngine = new RunEngine(this.context, this.runState, this.playerState);
    this.macroEngine = new MacroEngine(this.context, this.macroRunner);
    this.initialized = true;
  }

  public FabricContext context() {
    return this.context;
  }

  public RunState runState() {
    return this.runState;
  }

  public MacroRunner macroRunner() {
    return this.macroRunner;
  }

  public MacroRepository macroRepository() {
    return this.macroRepository;
  }

  public FabricRunRepository runRepository() {
    return this.runRepository;
  }
}
