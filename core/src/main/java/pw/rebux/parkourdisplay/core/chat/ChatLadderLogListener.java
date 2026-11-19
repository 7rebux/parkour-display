package pw.rebux.parkourdisplay.core.chat;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.event.Subscribe;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.ladder.LadderAnalysis;
import pw.rebux.parkourdisplay.core.ladder.LadderClimbEvent;
import pw.rebux.parkourdisplay.core.ladder.LadderStrategyFormatter;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

@RequiredArgsConstructor
public final class ChatLadderLogListener {

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onLadderClimb(LadderClimbEvent event) {
    if (!this.addon.configuration().analyzeLadderClimbs().enabled().get()) {
      return;
    }

    var climb = event.climb();
    if (!climb.isClimb()) {
      return;
    }

    this.addon.ladderExecutor().execute(() -> {
      var analysis = LadderAnalysis.of(climb,
          !this.addon.configuration().analyzeLadderClimbs().optimalIncludesClimbStart().get());
      this.addon.labyAPI().minecraft().executeNextTick(() -> this.print(analysis));
    });
  }

  private void print(LadderAnalysis analysis) {
    var climb = analysis.climb();
    var enterTick = climb.enterTick();
    var exitTick = climb.exitTick();

    var settings = this.addon.configuration().analyzeLadderClimbs();
    var format = this.addon.configuration().decimalFormat();
    var climbStart = climb.climbStartTick();

    ChatMessage.of("messages.climb.duration")
        .withColor(NamedTextColor.GRAY)
        .withArgs(
            Component.text(climb.airtime(), NamedTextColor.WHITE),
            Component.text(enterTick, NamedTextColor.AQUA),
            Component.text(exitTick - enterTick, NamedTextColor.GOLD),
            Component.text(climb.airtime() - exitTick, NamedTextColor.RED)
        )
        .send();

    if (settings.checkSneaking().get() && climb.isSneakMissed()) {
      ChatMessage.of("messages.climb.sneakMissed")
          .withColor(NamedTextColor.RED)
          .send();
    }

    var betterClimbStart = analysis.betterClimbStartTick();
    if (settings.checkClimbStart().get() && climbStart != -1) {
      var vy = Component.text(format.formatted(climb.vy(climbStart)), NamedTextColor.GOLD);
      if (analysis.climbStartDelayHelpful()) {
        ChatMessage.of("messages.climb.delay")
            .withColor(NamedTextColor.GREEN)
            .withArgs(Component.text(climbStart, NamedTextColor.WHITE), vy)
            .send();
      } else if (betterClimbStart != null) {
        // Pressing before the contact tick has no effect, so starting on it is a jam.
        var timing = LadderStrategyFormatter.start(
            betterClimbStart == climb.firstClimbStartTick() ? null : betterClimbStart - 1);
        ChatMessage.of("messages.climb.delayMissed")
            .withColor(NamedTextColor.RED)
            .withArgs(
                timing.color(NamedTextColor.GOLD),
                Component.text(betterClimbStart, NamedTextColor.WHITE),
                Component.text(climbStart, NamedTextColor.WHITE),
                vy
            )
            .send();
      }
    }

    if (settings.checkMidClimbReleases().get()) {
      if (climb.isReleaseHelpful()) {
        ChatMessage.of("messages.climb.release")
            .withColor(NamedTextColor.GREEN)
            .send();
      } else if (climb.isReleaseMissed()) {
        ChatMessage.of("messages.climb.releaseMissed")
            .withColor(NamedTextColor.RED)
            .send();
      }
    }

    if (settings.checkEarlyExit().get()) {
      if (climb.isEarlyExit()) {
        ChatMessage.of("messages.climb.earlyExit")
            .withColor(NamedTextColor.GREEN)
            .send();
      } else if (climb.isEarlyExitMissed()) {
        ChatMessage.of("messages.climb.earlyExitMissed")
            .withColor(NamedTextColor.RED)
            .send();
      }
    }

    var optimal = analysis.optimalStrategies();
    if (settings.showOptimalStrategy().get() && !optimal.isEmpty()) {
      var optimalAirtime = optimal.getFirst().ticks();

      if (climb.airtime() <= optimalAirtime) {
        ChatMessage.of("messages.climb.optimal")
            .withColor(NamedTextColor.GREEN)
            .withArgs(Component.text(climb.airtime(), NamedTextColor.WHITE))
            .send();
      } else {
        var strategies = Component.empty();
        for (int i = 0; i < optimal.size(); i++) {
          if (i > 0) strategies.append(Component.text(" / ", NamedTextColor.GRAY));
          strategies.append(LadderStrategyFormatter.describe(optimal.get(i)));
        }
        ChatMessage.of("messages.climb.notOptimal")
            .withColor(NamedTextColor.RED)
            .withArgs(
                Component.text(optimalAirtime, NamedTextColor.GREEN),
                strategies.color(NamedTextColor.WHITE))
            .send();
      }
    }
  }
}
