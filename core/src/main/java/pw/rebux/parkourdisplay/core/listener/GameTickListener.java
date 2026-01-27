package pw.rebux.parkourdisplay.core.listener;

import static net.labymod.api.client.component.Component.text;
import static net.labymod.api.client.component.Component.translatable;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.entity.player.GameMode;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Priority;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.state.TickPosition;

@RequiredArgsConstructor
public final class GameTickListener {

  private final ParkourDisplayAddon addon;

  private final TickPosition lastTick = new TickPosition();
  private int airTime = 0;
  private int groundTime = 0;

  @Subscribe(Priority.FIRST)
  public void onGameTick(GameTickEvent event) {
    var minecraft = this.addon.labyAPI().minecraft();
    var player = minecraft.getClientPlayer();

    if (event.phase() == Phase.PRE
        || player == null
        || player.isAbilitiesFlying()
        || player.gameMode() == GameMode.SPECTATOR
        || minecraft.isPaused()
    ) {
      return;
    }

    final var onGround = player.isOnGround();

    if (lastTick.onGround() && onGround) {
      groundTime = Math.min(groundTime + 1, Integer.MAX_VALUE);
    }

    // If the player landed this tick or is still airborne, we increase the air time
    if (!lastTick.onGround() || !onGround) {
      airTime++;
    }

    // Player jumped in this tick
    if (airTime == 1) {
      if (addon.configuration().showGroundDurations().get()) {
        var color = groundTime > 0 ? NamedTextColor.RED : NamedTextColor.GREEN;
        addon.displayMessage(
            text("%dt".formatted(groundTime), color)
                .append(text(" "))
                .append(translatable("parkourdisplay.labels.ground_time", NamedTextColor.GRAY)));
      }

      groundTime = 0;
    }

    // Player landed in this tick
    if (onGround && !lastTick.onGround()) {
      if (addon.configuration().showJumpDurations().get()) {
        addon.displayMessage(
            text("%dt".formatted(airTime), NamedTextColor.GOLD)
                .append(text(" "))
                .append(translatable("parkourdisplay.labels.air_time", NamedTextColor.GRAY)));
      }
    }

    /* EVERYTHING UNDER HERE WILL UPDATE VALUES FOR THE NEXT CALCULATIONS */

    if (onGround && airTime > 0) {
      airTime = 0;
    }

    lastTick.onGround(onGround);
  }
}
