package pw.rebux.parkourdisplay.core.platform;

import java.nio.file.Path;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.api.Permissions;
import pw.rebux.parkourdisplay.common.platform.ChatOutput;
import pw.rebux.parkourdisplay.common.platform.InputController;
import pw.rebux.parkourdisplay.common.platform.ParkourContext;
import pw.rebux.parkourdisplay.common.platform.PlayerAccess;
import pw.rebux.parkourdisplay.common.platform.RunConfig;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

/// LabyMod implementation of the shared [ParkourContext] services object.
public final class LabyContext implements ParkourContext {

  private final ParkourDisplayAddon addon;
  private final InputController input;
  private final ChatOutput chat;
  private final RunConfig config;

  public LabyContext(
      ParkourDisplayAddon addon,
      InputController input,
      ChatOutput chat,
      RunConfig config
  ) {
    this.addon = addon;
    this.input = input;
    this.chat = chat;
    this.config = config;
  }

  @Override
  public @Nullable PlayerAccess player() {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    return player == null ? null : new LabyPlayerAccess(player);
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
    return this.addon.labyAPI().permissionRegistry().isPermissionEnabled(Permissions.RUN_MACROS);
  }

  @Override
  public boolean isGamePaused() {
    return this.addon.labyAPI().minecraft().isPaused();
  }

  @Override
  public float partialTicks() {
    return this.addon.labyAPI().minecraft().getPartialTicks();
  }

  @Override
  public Path dataDir() {
    return ParkourDisplayAddon.DATA_DIR.toPath();
  }
}
