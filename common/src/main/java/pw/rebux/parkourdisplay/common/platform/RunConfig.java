package pw.rebux.parkourdisplay.common.platform;

import pw.rebux.parkourdisplay.common.macro.MacroRotationChange;

/// The subset of configuration the run/macro domain reads. Implementations back this with the
/// platform's settings system (LabyMod `ConfigProperty`s, a Fabric JSON config).
public interface RunConfig {

  int chatDecimalPlaces();

  boolean formatTicks();

  boolean showRunSplitsInChat();

  boolean showRunFinishOffsets();

  boolean smoothRotation();

  MacroRotationChange rotationChange();
}
