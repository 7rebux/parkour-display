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
    if (event.phase() != Phase.POST) {
      return;
    }

    var state = this.addon.playerState();
    var showGroundDurations = this.addon.configuration().showGroundDurations().get();
    var showJumpDurations = this.addon.configuration().showJumpDurations().get();

    if (showGroundDurations && state.isJumpTick()) {
      this.logGroundTime(state.groundTime());
    }

    if (showJumpDurations && state.isLandTick()) {
      this.logAirTime(state.airTime());
    }
  }

  private void logGroundTime(long value) {
    var color = value > 0 ? NamedTextColor.RED : NamedTextColor.GREEN;
    this.addon.displayMessage(
        text("%dt".formatted(value), color)
          .append(space())
          .append(translatable("parkourdisplay.labels.ground_time", NamedTextColor.GRAY)));
  }

  // TODO: Move args to translation string
  private void logAirTime(long value) {
    this.addon.displayMessage(
        text("%dt".formatted(value), NamedTextColor.GOLD)
          .append(space())
          .append(translatable("parkourdisplay.labels.air_time", NamedTextColor.GRAY)));
  }
}
