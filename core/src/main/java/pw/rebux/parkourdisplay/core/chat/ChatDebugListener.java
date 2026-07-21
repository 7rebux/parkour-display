package pw.rebux.parkourdisplay.core.chat;

import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

/// [Blip](https://www.mcpk.wiki/wiki/Blip)
/// [Stepping](https://www.mcpk.wiki/wiki/Stepping)
/// [Jump Cancel](https://www.mcpk.wiki/wiki/Jump_Cancel)
@RequiredArgsConstructor
public final class ChatDebugListener {

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onTick(GameTickEvent event) {
    var showJumpCancels = this.addon.configuration().showJumpCancels().get();

    if (event.phase() != Phase.POST) {
      return;
    }

    var state = this.addon.playerState();
    var isGrind = state.airTime() == 1
        && state.currentTick().y() == state.lastTick().y()
        && state.vy() == 0;

    if (showJumpCancels && isGrind) {
      ChatMessage.of("messages.stepping.jumpCancel").send();
    }
  }
}
