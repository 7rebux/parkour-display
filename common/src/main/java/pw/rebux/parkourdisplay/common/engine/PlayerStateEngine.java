package pw.rebux.parkourdisplay.common.engine;

import lombok.RequiredArgsConstructor;
import pw.rebux.parkourdisplay.common.platform.PlayerAccess;
import pw.rebux.parkourdisplay.common.state.PlayerState;
import pw.rebux.parkourdisplay.common.state.PlayerSnapshot;

/// Computes reusable player state shared across the addon. The platform calls [#beginTick] before
/// every other per-tick consumer and [#endTick] after all of them (LabyMod `Priority.FIRST` /
/// `Priority.LATEST`).
@RequiredArgsConstructor
public final class PlayerStateEngine {

  private final PlayerState state;

  /// Runs before all other tick consumers: snapshots the current tick and updates air/ground time.
  public void beginTick(PlayerAccess player) {
    this.state.currentTick(PlayerSnapshot.of(player));

    // If the player landed this tick or is still airborne, we increase the air time
    if (!this.state.lastTick().onGround() || !player.onGround()) {
      this.state.airTime(this.state.airTime() + 1);
    }

    // If the player is still on ground, we increase the ground time
    if (this.state.lastTick().onGround() && this.state.currentTick().onGround()) {
      this.state.groundTime(this.state.groundTime() + 1);
    }

    if (this.state.isLandTick()) {
      this.state.groundTime(0);
    }
  }

  /// Runs after all other tick consumers: snapshots the end-of-tick position.
  public void endTick(PlayerAccess player) {
    this.state.lastTick(PlayerSnapshot.of(player));

    if (player.onGround()) {
      this.state.airTime(0);
    }
  }
}
