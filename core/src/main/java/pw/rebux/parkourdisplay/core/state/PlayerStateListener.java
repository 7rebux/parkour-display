package pw.rebux.parkourdisplay.core.state;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.entity.player.ClientPlayer;
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
    currentTick.onGround(player.isOnGround());
    currentTick.movingForward(player.getForwardMovingSpeed() != 0);
    currentTick.movingSideways(tryGetMovingSideways(player));

    // If the player landed this tick or is still airborne, we increase the air time
    if (!state.lastTick().onGround() || !player.isOnGround()) {
      state.airTime(state.airTime() + 1);
    }

    if (state.lastTick().onGround() && state.currentTick().onGround()) {
      state.groundTime(state.groundTime() + 1);
    }

    // Player landed in this tick
    if (!state.lastTick().onGround() && state.currentTick().onGround()) {
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
    lastTick.onGround(player.isOnGround());
    lastTick.movingForward(player.getForwardMovingSpeed() != 0);
    lastTick.movingSideways(tryGetMovingSideways(player));

    if (player.isOnGround()) {
      state.airTime(0);
    }
  }

  private boolean tryGetMovingSideways(ClientPlayer player) {
    try {
      return player.getStrafeMovingSpeed() != 0;
    } catch (Throwable throwable) {
      return false;
    }
  }
}
