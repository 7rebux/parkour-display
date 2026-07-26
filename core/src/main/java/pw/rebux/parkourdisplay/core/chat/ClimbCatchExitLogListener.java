package pw.rebux.parkourdisplay.core.chat;

import java.util.Comparator;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.ladderbox.LadderBox;
import pw.rebux.parkourdisplay.core.util.BoundingBoxUtils;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

/// A less spammy variant of {@link ChatLadderYLogListener}: logs only the tick you catch a
/// climbable (with Y velocity and airtime), then every tick after you leave it until landing.
@RequiredArgsConstructor
public final class ClimbCatchExitLogListener {

  private final ParkourDisplayAddon addon;

  /// Whether we left a climbable and are still logging ticks until we hit the ground.
  private boolean loggingExit = false;

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    if (event.phase() != Phase.POST) {
      return;
    }

    if (!this.addon.configuration().showClimbCatchAndExit().get()) {
      this.loggingExit = false;
      return;
    }

    var state = this.addon.playerState();
    var onClimbable = state.currentTick().onClimbable();
    var wasClimbable = state.lastTick().onClimbable();
    var format = this.addon.configuration().decimalFormat();

    // On the ladder: never spam the middle ticks, only log the catch (first climbing tick).
    if (onClimbable) {
      this.loggingExit = false;

      if (!wasClimbable) {
        var vy = state.vy();
        ChatMessage.of("messages.climb.catch")
            .prefix(false)
            .withColor(NamedTextColor.LIGHT_PURPLE)
            .withArgs(
                Component.text(format.formatted(state.currentTick().y()), NamedTextColor.AQUA),
                Component.text(format.formatted(vy), this.deltaColor(vy)),
                Component.text(String.valueOf(state.airTime()), NamedTextColor.GRAY)
            )
            .send();

        this.sendCatchOffsets(state.currentTick().playerBoundingBox(),
            state.lastTick().playerBoundingBox(), format);
      }

      return;
    }

    // Just left the ladder: start logging the remaining airborne ticks until we land.
    if (wasClimbable) {
      this.loggingExit = true;
    }

    if (!this.loggingExit) {
      return;
    }

    var vy = state.vy();
    ChatMessage.of("messages.climb.exit")
        .prefix(false)
        .withArgs(
            Component.text(String.valueOf(state.airTime()), NamedTextColor.GRAY),
            Component.text(format.formatted(state.currentTick().y()), NamedTextColor.AQUA),
            Component.text(format.formatted(vy), this.deltaColor(vy))
        )
        .send();

    // Stop once we are back on the ground.
    if (state.currentTick().onGround()) {
      this.loggingExit = false;
    }
  }

  /// Reports how far into the ladder's intersection box we landed the catch, and how far off
  /// we still were on the previous tick - mirroring the run finish-split offsets.
  private void sendCatchOffsets(
      AxisAlignedBoundingBox catchBox,
      AxisAlignedBoundingBox previousBox,
      String format
  ) {
    var ladderBox = this.findCaughtBox(catchBox);

    if (ladderBox.isEmpty()) {
      return;
    }

    var intersectionBox = ladderBox.get().intersectionBox();
    var hit = BoundingBoxUtils.computeOverlap(catchBox, intersectionBox);
    var miss = BoundingBoxUtils.computeOverlap(previousBox, intersectionBox);

    ChatMessage.of("messages.climb.catchOffsetHit")
        .prefix(false)
        .withColor(NamedTextColor.GREEN)
        .withArgs(
            Component.text(format.formatted(hit.getX()), NamedTextColor.DARK_GREEN),
            Component.text(format.formatted(hit.getZ()), NamedTextColor.DARK_GREEN)
        )
        .send();

    ChatMessage.of("messages.climb.catchOffsetMiss")
        .prefix(false)
        .withColor(NamedTextColor.RED)
        .withArgs(
            Component.text(format.formatted(miss.getX()), NamedTextColor.DARK_RED),
            Component.text(format.formatted(miss.getZ()), NamedTextColor.DARK_RED)
        )
        .send();
  }

  /// Finds the registered ladder box we caught: the one whose intersection box overlaps the
  /// player the most on the XZ plane at the catch tick.
  private Optional<LadderBox> findCaughtBox(AxisAlignedBoundingBox catchBox) {
    return this.addon.ladderBoxRegistry().ladderBoxes().stream()
        .filter(box -> BoundingBoxUtils.intersectsXZ(catchBox, box.intersectionBox()))
        .max(Comparator.comparingDouble(box -> {
          var overlap = BoundingBoxUtils.computeOverlap(catchBox, box.intersectionBox());
          return Math.min(overlap.getX(), overlap.getZ());
        }));
  }

  private TextColor deltaColor(double delta) {
    return delta > 0
        ? NamedTextColor.GREEN
        : delta < 0 ? NamedTextColor.RED : NamedTextColor.GRAY;
  }
}
