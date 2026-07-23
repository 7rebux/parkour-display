package pw.rebux.parkourdisplay.core.chat;

import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

/// [Jump Cancel - MCPK Wiki](https://www.mcpk.wiki/wiki/Jump_Cancel)
@RequiredArgsConstructor
public final class ChatMovementLogListener {

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onTick(GameTickEvent event) {
    var showGrinds = this.addon.configuration().showGrinds().get();
    var state = this.addon.playerState();

    if (event.phase() != Phase.POST) {
      return;
    }

    // Note: It's possible to use the corner of a slab to jump-cancel without moving up
    // the slab itself. To do this, the collision must be X-facing.
    var isGrind = state.airTime() == 1
        && this.addon.minecraftInputUtil().jumpKey().isDown()
        && state.currentTick().y() == state.lastTick().y()
        && state.vy() == 0;

    if (showGrinds && isGrind) {
      ChatMessage.of("messages.movement.grind").send();
    }
  }
}
