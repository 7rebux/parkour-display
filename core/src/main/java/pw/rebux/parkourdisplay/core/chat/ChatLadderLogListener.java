package pw.rebux.parkourdisplay.core.chat;

import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

@RequiredArgsConstructor
public final class ChatLadderLogListener {

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    if (event.phase() != Phase.POST) {
      return;
    }

    var player = this.addon.labyAPI().minecraft().getClientPlayer();

    if (player == null) {
      return;
    }

    var state = this.addon.playerState();

    if (state.currentTick().onClimbable()) {
      // Catch ladder
      if (!state.lastTick().onClimbable()) {
        ChatMessage.of("Caught ladder at airtime=" + state.airTime()).send();
        ChatMessage.of("At y=" + state.currentTick().y()).send();
        ChatMessage.of("With vy=" + state.vy()).send();
      }
    }

    // Exit ladder
    if (!state.currentTick().onClimbable() && state.lastTick().onClimbable()) {
      ChatMessage.of("Exited with climb time=" + state.climbTime()).send();
      ChatMessage.of("Exited ladder at y=" + state.currentTick().y()).send();
    }
  }
}
