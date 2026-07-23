package pw.rebux.parkourdisplay.core.platform;

import pw.rebux.parkourdisplay.common.macro.MacroRotationChange;
import pw.rebux.parkourdisplay.common.platform.RunConfig;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

/// LabyMod-backed [RunConfig], reading the addon's [pw.rebux.parkourdisplay.core.ParkourDisplayConfiguration]
/// settings live on each access.
public final class LabyRunConfig implements RunConfig {

  private final ParkourDisplayAddon addon;

  public LabyRunConfig(ParkourDisplayAddon addon) {
    this.addon = addon;
  }

  @Override
  public int chatDecimalPlaces() {
    return this.addon.configuration().chatDecimalPlaces().get();
  }

  @Override
  public boolean formatTicks() {
    return this.addon.configuration().formatTicks().get();
  }

  @Override
  public boolean showRunSplitsInChat() {
    return this.addon.configuration().showRunSplitsInChat().get();
  }

  @Override
  public boolean showRunFinishOffsets() {
    return this.addon.configuration().showRunFinishOffsets().get();
  }

  @Override
  public boolean smoothRotation() {
    return this.addon.configuration().smoothRotation().get();
  }

  @Override
  public MacroRotationChange rotationChange() {
    return this.addon.configuration().rotationChange().get();
  }
}
