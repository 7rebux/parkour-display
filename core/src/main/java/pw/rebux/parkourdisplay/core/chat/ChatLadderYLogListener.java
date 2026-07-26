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
public final class ChatLadderYLogListener {

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();

    if (event.phase() != Phase.POST || player == null) {
      return;
    }

    if (!this.addon.configuration().showLadderYCoordinates().get()) {
      return;
    }

    var state = this.addon.playerState();

    // Log the whole airborne period: from the first tick off the ground (current tick
    // airborne) through the landing tick (last tick was still airborne). A fully grounded
    // tick has both flags set and is skipped.
    if (state.currentTick().onGround() && state.lastTick().onGround()) {
      return;
    }

    var format = this.addon.configuration().decimalFormat();
    var y = state.currentTick().y();
    var delta = state.vy();

    var deltaColor = delta > 0
        ? NamedTextColor.GREEN
        : delta < 0 ? NamedTextColor.RED : NamedTextColor.GRAY;

    // Flag ticks spent on a climbable so ladder sections stand out within the jump.
    var flag = state.currentTick().onClimbable()
        ? Component.text(" [climb]", NamedTextColor.GOLD)
        : Component.empty();

    ChatMessage.of("messages.ladder.y")
        .prefix(false)
        .withArgs(
            Component.text(String.valueOf(state.airTime()), NamedTextColor.GRAY),
            Component.text(format.formatted(y), NamedTextColor.AQUA),
            Component.text(format.formatted(delta), deltaColor),
            flag
        )
        .send();
  }
}
