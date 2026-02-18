package pw.rebux.parkourdisplay.core.state;

import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Priority;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

/**
 * Computes reusable player state which is shared across the addon.
 */
@RequiredArgsConstructor
public final class PlayerStateListener {

  private final ParkourDisplayAddon addon;

  // This listener must always run before all other listeners.
  // Because subsequent listeners depend on the state that is computed here.
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

    var currentTick = state.currentTick();
    currentTick.x(player.position().getX());
    currentTick.y(player.position().getY());
    currentTick.z(player.position().getZ());
    currentTick.yaw(player.getRotationYaw());
    currentTick.pitch(player.getRotationPitch());
    // TODO: I dont like this being inline 2 times
    currentTick.playerBoundingBox(player.axisAlignedBoundingBox().move(0, 0, 0));
    currentTick.onGround(player.isOnGround());

    // If the player landed this tick or is still airborne, we increase the air time
    if (!state.lastTick().onGround() || !player.isOnGround()) {
      state.airTime(state.airTime() + 1);
    }

    // If the player is still on ground, we increase the ground time
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

    var lastTick = state.lastTick();
    lastTick.x(player.position().getX());
    lastTick.y(player.position().getY());
    lastTick.z(player.position().getZ());
    lastTick.yaw(player.getRotationYaw());
    lastTick.pitch(player.getRotationPitch());
    lastTick.playerBoundingBox(player.axisAlignedBoundingBox().move(0, 0, 0));
    lastTick.onGround(player.isOnGround());

    if (player.isOnGround()) {
      state.airTime(0);
    }
  }
}
