package pw.rebux.parkourdisplay.core.chat;

import static net.labymod.api.client.component.Component.space;
import static net.labymod.api.client.component.Component.text;
import static net.labymod.api.client.component.Component.translatable;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@RequiredArgsConstructor
public final class ChatMoveTimeLogListener {

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    var state = this.addon.playerState();

    if (event.phase() != Phase.POST) {
      return;
    }

    // Player jumped in this tick
    if (state.lastTick().onGround() && !state.currentTick().onGround()) {
      this.logGroundTime(state.groundTime());
    }

    // Player landed in this tick
    if (state.currentTick().onGround() && !state.lastTick().onGround()) {
      this.logAirTime(state.airTime());
    }
  }

  private void logGroundTime(long value) {
    if (!this.addon.configuration().showGroundDurations().get()) {
      return;
    }

    var color = value > 0 ? NamedTextColor.RED : NamedTextColor.GREEN;
    this.addon.displayMessage(
        text("%dt".formatted(value), color)
          .append(space())
          .append(translatable("parkourdisplay.labels.ground_time", NamedTextColor.GRAY)));
  }

  private void logAirTime(long value) {
    if (!this.addon.configuration().showJumpDurations().get()) {
      return;
    }

    // TODO: Move args to translation string
    this.addon.displayMessage(
        text("%dt".formatted(value), NamedTextColor.GOLD)
          .append(space())
          .append(translatable("parkourdisplay.labels.air_time", NamedTextColor.GRAY)));
  }
}
