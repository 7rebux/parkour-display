package pw.rebux.parkourdisplay.core.state;

import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Priority;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.platform.LabyPlayerAccess;

/// Forwards LabyMod game ticks to the shared player-state engine, preserving the ordering the rest
/// of the addon depends on: the state is produced before all other consumers ({@link Priority#FIRST})
/// and finalised after all of them ({@link Priority#LATEST}).
@RequiredArgsConstructor
public final class PlayerStateListener {

  private final ParkourDisplayAddon addon;

  // This listener must always run before all other listeners.
  // Because subsequent listeners depend on the state computed here.
  @Subscribe(Priority.FIRST)
  public void onGameTickFirst(GameTickEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();

    if (player == null || event.phase() != Phase.POST) {
      return;
    }

    this.addon.playerStateEngine().beginTick(new LabyPlayerAccess(player));
  }

  // This listener must always run after all other listeners.
  @Subscribe(Priority.LATEST)
  public void onGameTickLatest(GameTickEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();

    if (player == null || event.phase() != Phase.POST) {
      return;
    }

    this.addon.playerStateEngine().endTick(new LabyPlayerAccess(player));
  }
}
