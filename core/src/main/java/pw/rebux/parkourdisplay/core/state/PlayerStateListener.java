package pw.rebux.parkourdisplay.core.state;

import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Priority;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.TickPosition;
import pw.rebux.parkourdisplay.core.util.WorldUtils;

/**
 * Computes reusable player state which is shared across the addon.
 */
@RequiredArgsConstructor
public final class PlayerStateListener {

  private final ParkourDisplayAddon addon;

  // This listener must always run before all other listeners.
  // Because subsequent listeners depend on the state computed here.
  @Subscribe(Priority.FIRST)
  public void onGameTickFirst(GameTickEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var state = this.addon.playerState();

    if (player == null) {
      return;
    }

    if (event.phase() != Phase.POST) {
      return;
    }

    // Important to set the current tick first
    state.currentTick(TickPosition.of(player));

    // If the player landed this tick or is still airborne, we increase the air time
    if (!state.lastTick().onGround() || !player.isOnGround()) {
      state.airTime(state.airTime() + 1);

      if (state.currentTick().onClimbable()) {
        state.climbTime(state.climbTime() + 1);
      }
    }

    // If the player is still grounded, we increase the ground time
    if (state.lastTick().onGround() && state.currentTick().onGround()) {
      state.groundTime(state.groundTime() + 1);
    }

    if (state.isLandTick()) {
      state.groundTime(0);
    }
  }

  // This listener must always run after all other listeners.
  @Subscribe(Priority.LATEST)
  public void onGameTickLatest(GameTickEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var state = this.addon.playerState();

    if (player == null) {
      return;
    }

    if (event.phase() != Phase.POST) {
      return;
    }

    state.lastTick(TickPosition.of(player));

    if (player.isOnGround()) {
      state.airTime(0);
    }

    // TODO: Actually first statement should be omitted?
    if (player.isOnGround() || !WorldUtils.onClimbable(player)) {
      state.climbTime(0);
    }
  }
}
