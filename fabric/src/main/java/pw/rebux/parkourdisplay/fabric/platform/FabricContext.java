package pw.rebux.parkourdisplay.fabric.platform;

import java.nio.file.Path;
import net.minecraft.client.MinecraftClient;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.common.platform.ChatOutput;
import pw.rebux.parkourdisplay.common.platform.InputController;
import pw.rebux.parkourdisplay.common.platform.ParkourContext;
import pw.rebux.parkourdisplay.common.platform.PlayerAccess;
import pw.rebux.parkourdisplay.common.platform.RunConfig;

/// Fabric implementation of the shared [ParkourContext] services object.
public final class FabricContext implements ParkourContext {

  private final MinecraftClient client;
  private final InputController input;
  private final ChatOutput chat;
  private final FabricRunConfig config;
  private final Path dataDir;

  public FabricContext(
      MinecraftClient client,
      InputController input,
      ChatOutput chat,
      FabricRunConfig config,
      Path dataDir
  ) {
    this.client = client;
    this.input = input;
    this.chat = chat;
    this.config = config;
    this.dataDir = dataDir;
  }

  @Override
  public @Nullable PlayerAccess player() {
    var player = this.client.player;
    if (player == null) {
      return null;
    }
    var gameMode = this.client.interactionManager == null
        ? null
        : this.client.interactionManager.getCurrentGameMode();
    return new FabricPlayerAccess(player, gameMode);
  }

  @Override
  public InputController input() {
    return this.input;
  }

  @Override
  public ChatOutput chat() {
    return this.chat;
  }

  @Override
  public RunConfig config() {
    return this.config;
  }

  @Override
  public boolean isMacrosAllowed() {
    return this.config.runMacros();
  }

  @Override
  public boolean isGamePaused() {
    return this.client.isPaused();
  }

  @Override
  public float partialTicks() {
    return this.client.getRenderTickCounter().getTickProgress(true);
  }

  @Override
  public Path dataDir() {
    return this.dataDir;
  }
}
