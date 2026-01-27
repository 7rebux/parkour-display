package pw.rebux.parkourdisplay.core.chat;

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

  // TODO: Duplicated..
  private long airTime = 0;
  private long groundTime = 0;

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    var state = this.addon.playerState();

    if (event.phase() != Phase.POST) {
      return;
    }

    if (state.lastTick().onGround() && state.currentTick().onGround()) {
      this.groundTime++;
    }

    // If the player landed this tick or is still airborne, we increase the air time
    if (!state.lastTick().onGround() || !state.currentTick().onGround()) {
      this.airTime++;
    }

    // Player jumped in this tick
    if (state.lastTick().onGround() && !state.currentTick().onGround()) {
      this.logGroundTime();
      this.groundTime = 0;
    }

    // Player landed in this tick
    if (state.currentTick().onGround() && !state.lastTick().onGround()) {
      this.logAirTime();
    }

    if (state.currentTick().onGround()) {
      this.airTime = 0;
    }
  }

  private void logGroundTime() {
    if (!this.addon.configuration().showGroundDurations().get()) {
      return;
    }

    var color = this.groundTime > 0 ? NamedTextColor.RED : NamedTextColor.GREEN;
    this.addon.displayMessage(
        text("%dt".formatted(this.groundTime), color)
            .append(text(" "))
            .append(translatable("parkourdisplay.labels.ground_time", NamedTextColor.GRAY)));
  }

  private void logAirTime() {
    if (!this.addon.configuration().showJumpDurations().get()) {
      return;
    }

    this.addon.displayMessage(
        text("%dt".formatted(this.airTime), NamedTextColor.GOLD)
            .append(text(" "))
            .append(translatable("parkourdisplay.labels.air_time", NamedTextColor.GRAY)));
  }
}
