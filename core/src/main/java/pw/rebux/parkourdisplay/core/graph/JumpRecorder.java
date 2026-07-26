package pw.rebux.parkourdisplay.core.graph;

import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

/// Buffers the per-tick vertical velocity of each jump so the last completed one can be exported
/// as a graph. Recording is always on (not gated by any config) so the command always has data.
///
/// A "jump" is the whole airborne period: from the first tick off the ground through the landing
/// tick - the same window {@code ChatLadderYLogListener} logs.
@RequiredArgsConstructor
public final class JumpRecorder {

  private final ParkourDisplayAddon addon;

  /// Ticks of the jump currently in progress.
  private final LinkedList<JumpSample> currentJump = new LinkedList<>();

  /// Snapshot of the last completed jump, kept until the next one finishes.
  private final LinkedList<JumpSample> lastJump = new LinkedList<>();

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();

    if (event.phase() != Phase.POST || player == null) {
      return;
    }

    var state = this.addon.playerState();

    // Start a fresh recording the moment we leave the ground, seeding a tick-0 baseline at the
    // ground height (lastTick is still the grounded tick here) so the graph starts at 0.
    if (state.isJumpTick()) {
      this.currentJump.clear();
      this.currentJump.addLast(new JumpSample(0, state.lastTick().y(), false));
    }

    // Skip fully grounded ticks; record every airborne tick including the landing tick.
    if (state.currentTick().onGround() && state.lastTick().onGround()) {
      return;
    }

    this.currentJump.addLast(new JumpSample(
        (int) state.airTime(),
        state.currentTick().y(),
        state.currentTick().onClimbable()
    ));

    // On landing, retain this jump as the last completed one (mirrors RunState.processFinish).
    if (state.isLandTick()) {
      this.lastJump.clear();
      this.lastJump.addAll(this.currentJump);
      this.currentJump.clear();
    }
  }

  /// @return the samples of the last completed jump, or an empty list if none has finished yet.
  public List<JumpSample> lastJump() {
    return this.lastJump;
  }
}
