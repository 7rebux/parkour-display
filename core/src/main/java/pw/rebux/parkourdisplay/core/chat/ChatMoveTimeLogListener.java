package pw.rebux.parkourdisplay.core.chat;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;
import pw.rebux.parkourdisplay.core.util.TickFormatter;

@RequiredArgsConstructor
public final class ChatMoveTimeLogListener {

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    if (event.phase() != Phase.POST) {
      return;
    }

    var state = this.addon.playerState();
    var formatTicks = this.addon.configuration().formatTicks().get();
    var showGroundDurations = this.addon.configuration().showGroundDurations().get();
    var showClimbDurations = this.addon.configuration().showClimbDurations().get();
    var showJumpDurations = this.addon.configuration().showJumpDurations().get();

    if (showJumpDurations && state.isLandTick()) {
      ChatMessage.of("messages.moveTime.air")
          .withArgs(TickFormatter.format(state.airTime(), formatTicks))
          .prefix(false)
          .send();
    }

    if (showClimbDurations && state.isLandTick() && state.climbTime() > 0) {
      ChatMessage.of("messages.moveTime.climbable")
          .withArgs(TickFormatter.format(state.climbTime(), formatTicks))
          .prefix(false)
          .send();
    }

    if (showGroundDurations && state.isJumpTick()) {
      ChatMessage.of("messages.moveTime.ground")
          .withArgs(TickFormatter.format(state.groundTime(), formatTicks))
          .prefix(false)
          .withColor(state.groundTime() > 0 ? NamedTextColor.RED : NamedTextColor.GREEN)
          .send();
    }
  }
}
