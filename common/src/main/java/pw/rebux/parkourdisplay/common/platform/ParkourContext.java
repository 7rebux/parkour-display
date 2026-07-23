package pw.rebux.parkourdisplay.common.platform;

import java.nio.file.Path;
import org.jspecify.annotations.Nullable;

/// The single services object the run/macro engines receive instead of reaching through a
/// platform-specific addon. Each platform provides one implementation.
public interface ParkourContext {

  /// @return the local player, or null when unavailable (no world loaded, etc.).
  @Nullable
  PlayerAccess player();

  InputController input();

  ChatOutput chat();

  RunConfig config();

  /// @return whether macro playback is permitted (LabyMod permission / Fabric config flag).
  boolean isMacrosAllowed();

  boolean isGamePaused();

  /// @return the render sub-tick fraction in [0, 1] for smooth rotation interpolation.
  float partialTicks();

  /// @return the base directory for saved runs and macros.
  Path dataDir();
}
