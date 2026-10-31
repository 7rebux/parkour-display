package pw.rebux.parkourdisplay.core.chat;

import java.util.Locale;
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
    if (!this.addon.configuration().showClimbCatchAndExit().get()) {
      return;
    }

    var climb = event.climb();
    this.addon.ladderExecutor().execute(() -> {
      var analysis = LadderAnalysis.of(climb);
      this.addon.labyAPI().minecraft().executeNextTick(() -> this.print(analysis));
    });
  }

  private void print(LadderAnalysis analysis) {
    var climb = analysis.climb();
    var enterTick = climb.enterTick();
    var exitTick = climb.exitTick();

    ChatMessage.of("messages.climb.duration")
        .withColor(NamedTextColor.GRAY)
        .withArgs(
            Component.text(enterTick, NamedTextColor.AQUA),
            Component.text(exitTick - enterTick, NamedTextColor.GOLD),
            Component.text(climb.airtime() - exitTick, NamedTextColor.RED),
            Component.text(climb.airtime(), NamedTextColor.WHITE)
        )
        .send();

    var climbStart = climb.climbStartTick();
    if (climbStart != -1) {
      var format = this.addon.configuration().decimalFormat();
      ChatMessage.of("messages.climb.start")
          .withColor(NamedTextColor.GRAY)
          .withArgs(
              Component.text(climbStart, NamedTextColor.WHITE),
              Component.text(format.formatted(climb.y(climbStart)), NamedTextColor.AQUA),
              Component.text(format.formatted(climb.vy(climbStart)), NamedTextColor.GOLD)
          )
          .send();
    }

    var betterClimbStart = analysis.betterClimbStartTick();
    if (analysis.climbStartDelayHelpful()) {
      ChatMessage.of("messages.climb.delay")
          .withColor(NamedTextColor.GREEN)
          .withArgs(Component.text(climbStart, NamedTextColor.WHITE))
          .send();
    } else if (betterClimbStart != null) {
      // Pressing before the contact tick has no effect, so starting on it is a jam.
      var timing = LadderStrategyFormatter.start(
          betterClimbStart == enterTick ? null : betterClimbStart - 1);
      ChatMessage.of("messages.climb.delayMissed")
          .withColor(NamedTextColor.RED)
          .withArgs(
              timing.color(NamedTextColor.GOLD),
              Component.text(betterClimbStart, NamedTextColor.WHITE),
              Component.text(climbStart, NamedTextColor.WHITE)
          )
          .send();
    }

    if (climb.isReleaseHelpful()) {
      ChatMessage.of("messages.climb.release")
          .withColor(NamedTextColor.GREEN)
          .send();
    } else if (climb.isReleaseMissed()) {
      ChatMessage.of("messages.climb.releaseMissed")
          .withColor(NamedTextColor.RED)
          .send();
    }

    if (climb.isEarlyExit()) {
      ChatMessage.of("messages.climb.earlyExit")
          .withColor(NamedTextColor.GREEN)
          .send();
    } else if (climb.isEarlyExitMissed()) {
      ChatMessage.of("messages.climb.earlyExitMissed")
          .withColor(NamedTextColor.RED)
          .send();
    }

    var optimal = analysis.optimalStrategies();
    if (!optimal.isEmpty()) {
      var optimalAirtime = optimal.getFirst().ticks();

      if (climb.airtime() <= optimalAirtime) {
        ChatMessage.of("messages.climb.optimal")
            .withColor(NamedTextColor.GREEN)
            .withArgs(Component.text(climb.airtime(), NamedTextColor.WHITE))
            .send();
      } else {
        ChatMessage.of("messages.climb.notOptimal")
            .withColor(NamedTextColor.RED)
            .withArgs(
                Component.text(optimalAirtime, NamedTextColor.GREEN),
                Component.text(climb.airtime(), NamedTextColor.WHITE))
            .send();

        for (var strategy : optimal) {
          ChatMessage.of("messages.climb.optimalStrategy")
              .prefix(false)
              .withColor(NamedTextColor.GRAY)
              .withArgs(
                  LadderStrategyFormatter.describe(strategy).color(NamedTextColor.WHITE),
                  Component.text(
                      String.format(Locale.ROOT, "%+.4f", strategy.peakHeight()),
                      NamedTextColor.YELLOW))
              .send();
        }
      }
    }
  }
}
