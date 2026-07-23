package pw.rebux.parkourdisplay.fabric.platform;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import pw.rebux.parkourdisplay.common.macro.MacroRotationChange;
import pw.rebux.parkourdisplay.common.platform.RunConfig;

/// Fabric-backed [RunConfig], persisted as `config/parkourdisplay.json`. Defaults mirror the
/// LabyMod addon. Macro playback is gated by a plain config flag (there is no Fabric permission
/// system equivalent to LabyMod's).
public final class FabricRunConfig implements RunConfig {

  /// Plain settings holder serialized by Gson.
  static final class Settings {
    int chatDecimalPlaces = 3;
    boolean formatTicks = true;
    boolean showRunSplitsInChat = true;
    boolean showRunFinishOffsets = false;
    boolean smoothRotation = true;
    MacroRotationChange rotationChange = MacroRotationChange.Absolute;
    boolean runMacros = true;
  }

  private final Settings settings;

  public FabricRunConfig(Path configDir, Gson gson) {
    var file = configDir.resolve("parkourdisplay.json");
    Settings loaded = null;

    if (Files.exists(file)) {
      try {
        loaded = gson.fromJson(Files.readString(file), Settings.class);
      } catch (IOException | RuntimeException ignored) {
        // Fall back to defaults on any read/parse failure.
      }
    }

    this.settings = loaded != null ? loaded : new Settings();

    if (!Files.exists(file)) {
      try {
        Files.createDirectories(configDir);
        Files.writeString(file, gson.toJson(this.settings));
      } catch (IOException ignored) {
        // Non-fatal: run with in-memory defaults.
      }
    }
  }

  public boolean runMacros() {
    return this.settings.runMacros;
  }

  @Override
  public int chatDecimalPlaces() {
    return this.settings.chatDecimalPlaces;
  }

  @Override
  public boolean formatTicks() {
    return this.settings.formatTicks;
  }

  @Override
  public boolean showRunSplitsInChat() {
    return this.settings.showRunSplitsInChat;
  }

  @Override
  public boolean showRunFinishOffsets() {
    return this.settings.showRunFinishOffsets;
  }

  @Override
  public boolean smoothRotation() {
    return this.settings.smoothRotation;
  }

  @Override
  public MacroRotationChange rotationChange() {
    return this.settings.rotationChange;
  }
}
