package pw.rebux.parkourdisplay.core.chat;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

@RequiredArgsConstructor
public final class ChatLadderLogListener {

  private final ParkourDisplayAddon addon;

  private long lastEnterAirtime = 0; // TODO: Das sagt ja nur wann man in der box ist, nicht wann w und so
  private double lastEnterY = 0;
  private double lastEnterVy = 0; // TODO: Das ist nicht so wichtig weil das ist vy wenn in box, nicht vy wenn start climb

  private long lastExitAirtime = 0;
  private long lastExitClimbTime = 0; // TODO: Climb time ist halt auch einfach falsch, weil das ist time in box nicht climbtime
  private double lastExitPrevY = 0;
  private double lastExitY = 0;

  private boolean awaitingOnGround = false;

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

    // Enter ladder
    if (state.currentTick().onClimbable() && !state.lastTick().onClimbable()) {
      this.lastEnterAirtime = state.airTime();
      this.lastEnterY = state.currentTick().y();
      this.lastEnterVy = state.vy();
    }

    // Exit ladder
    if (!state.currentTick().onClimbable() && state.lastTick().onClimbable()) {
      this.lastExitAirtime = state.airTime();
      this.lastExitClimbTime = state.climbTime();
      this.lastExitY = state.currentTick().y();
      this.lastExitPrevY = state.lastTick().y();
      this.awaitingOnGround = true;
    }

    if (state.currentTick().onGround() && this.awaitingOnGround) {
      this.awaitingOnGround = false;

      ChatMessage.of("messages.climb.duration")
          .withColor(NamedTextColor.GRAY)
          .withArgs(
              Component.text(this.lastEnterAirtime, NamedTextColor.AQUA),
              Component.text(this.lastExitClimbTime, NamedTextColor.GOLD),
              Component.text(state.airTime() - this.lastExitAirtime, NamedTextColor.RED),
              Component.text(state.airTime(), NamedTextColor.WHITE)
          )
          .send();

      ChatMessage.of(
          "Ladder stats: enterY=%f enterVy=%f exitY=%f"
              .formatted(this.lastEnterY, this.lastEnterVy, this.lastExitY)
      ).send();
    }
  }
}
