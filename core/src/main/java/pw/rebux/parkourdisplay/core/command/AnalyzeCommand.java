package pw.rebux.parkourdisplay.core.command;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextDecoration;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.ladder.LadderSolver;
import pw.rebux.parkourdisplay.core.ladder.LadderStrategy;
import pw.rebux.parkourdisplay.core.ladder.LadderStrategyFormatter;
import pw.rebux.parkourdisplay.core.util.ChatMessage;
import pw.rebux.parkourdisplay.core.util.WorldUtils;

public final class AnalyzeCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public AnalyzeCommand(ParkourDisplayAddon addon) {
    super("analyze");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var world = this.addon.labyAPI().minecraft().clientWorld();
    var player = Objects.requireNonNull(this.addon.labyAPI().minecraft().getClientPlayer());
    var hitResult = WorldUtils.rayTraceHit(64.0D, 1.0F)
        .filter(hit -> WorldUtils.isClimbable(world.getBlockState(hit.location())));

    if (hitResult.isEmpty()) {
      ChatMessage.of(this, "invalidBlock")
          .withColor(NamedTextColor.RED)
          .send();
      return true;
    }

    var ladders = WorldUtils.findConnectedLadders(hitResult.get().location());
    var ladderStartY = ladders.stream()
        .mapToDouble(AxisAlignedBoundingBox::getMinY)
        .min()
        .orElseThrow();
    var ladderEndY = ladders.stream()
        .mapToDouble(AxisAlignedBoundingBox::getMaxY)
        .max()
        .orElseThrow();
    var startY = player.position().getY();

    this.addon.ladderExecutor().execute(() -> {
      var strategies = LadderSolver.strategies(startY, ladderStartY, ladderEndY);
      this.addon.labyAPI().minecraft().executeNextTick(
          () -> this.print(strategies, startY, ladderStartY, ladderEndY));
    });

    return true;
  }

  private void print(
      List<LadderStrategy> strategies,
      double startY,
      double ladderStartY,
      double ladderEndY
  ) {
    if (strategies.isEmpty()) {
      ChatMessage.of(this, "unreachable")
          .withColor(NamedTextColor.RED)
          .send();
      return;
    }

    var bar = Component.text(
        " ".repeat(20), NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH);
    this.addon.displayMessage(Component.empty()
        .append(bar)
        .append(Component.text(" "))
        .append(message("title").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
        .append(Component.text(" "))
        .append(bar));

    ChatMessage.of(this, "info")
        .prefix(false)
        .withColor(NamedTextColor.GRAY)
        .withArgs(
            Component.text(format(ladderStartY), NamedTextColor.WHITE),
            Component.text(format(ladderEndY), NamedTextColor.WHITE),
            Component.text(format(startY), NamedTextColor.WHITE))
        .send();

    ChatMessage.of(this, "row")
        .prefix(false)
        .withColor(NamedTextColor.DARK_GRAY)
        .withArgs(
            message("airtime").color(NamedTextColor.GREEN),
            message("start").color(NamedTextColor.AQUA),
            message("climb").color(NamedTextColor.LIGHT_PURPLE),
            message("overshoot").color(NamedTextColor.GOLD),
            message("peak").color(NamedTextColor.YELLOW),
            message("strategy").color(NamedTextColor.WHITE))
        .send();

    var fastest = strategies.getFirst().ticks();

    for (var strategy : strategies) {
      var best = strategy.ticks() == fastest;

      ChatMessage.of(this, "row")
          .prefix(false)
          .withColor(NamedTextColor.DARK_GRAY)
          .withArgs(
              message("ticks", strategy.ticks())
                  .color(best ? NamedTextColor.GREEN : NamedTextColor.RED),
              Component.text(strategy.startTicks(), NamedTextColor.AQUA),
              Component.text(strategy.climbTicks(), NamedTextColor.LIGHT_PURPLE),
              Component.text(strategy.overshootTicks(), NamedTextColor.GOLD),
              Component.text(
                  String.format(Locale.ROOT, "%+.4f", strategy.peakHeight()),
                  NamedTextColor.YELLOW),
              LadderStrategyFormatter.describe(strategy).color(best ? NamedTextColor.WHITE : NamedTextColor.GRAY))
          .send();
    }

    this.addon.displayMessage(
        Component.text(" ".repeat(69), NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));
  }

  private static String format(double value) {
    return new DecimalFormat("0.####", DecimalFormatSymbols.getInstance(Locale.ROOT))
        .format(value);
  }

  private Component message(String key, Object... args) {
    return Component.translatable()
        .key("%s.commands.%s.messages.%s"
            .formatted(ParkourDisplayAddon.NAMESPACE, getPrefix(), key))
        .arguments(Arrays.stream(args)
            .map(arg -> (Component) Component.text(arg.toString()))
            .toList())
        .build();
  }
}
