package pw.rebux.parkourdisplay.core.ladder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.labymod.api.client.component.Component;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LadderStrategyFormatter {

  /// @return the inputs of a strategy, e.g. "pessi 3t + early exit on tick 10".
  public static Component describe(LadderStrategy strategy) {
    if (!strategy.climbNeeded()) return message("noClimb");
    if (strategy.isPlainHold()) return message("plainHold");

    var parts = new ArrayList<Component>();
    parts.add(start(strategy.pessi()));
    strategy.releaseTicks().forEach(tick -> parts.add(message("release")));
    if (strategy.earlyExitTick() != null) parts.add(message("earlyExit", strategy.earlyExitTick()));

    var result = Component.empty();
    for (int i = 0; i < parts.size(); i++) {
      if (i > 0) result.append(Component.text(" + "));
      result.append(parts.get(i));
    }
    return result;
  }

  /// @param pessi ticks between the jump and the first climb input, or null for a jam.
  public static Component start(@Nullable Integer pessi) {
    if (pessi == null) return message("jam");
    if (pessi == 1) return message("maxPessi");
    return message("pessi", pessi);
  }

  /// @return the tricks joined, e.g. "sneak + early exit".
  public static Component tricks(Set<MissedTrick> tricks) {
    var result = Component.empty();
    var first = true;
    for (var trick : tricks) {
      if (!first) result.append(Component.text(" + "));
      result.append(translatable("trick." + trick.name().toLowerCase(Locale.ROOT)));
      first = false;
    }
    return result;
  }

  private static Component message(String key, Object... args) {
    return translatable("strategy." + key, args);
  }

  private static Component translatable(String key, Object... args) {
    return Component.translatable()
        .key("%s.messages.ladder.%s".formatted(ParkourDisplayAddon.NAMESPACE, key))
        .arguments(Arrays.stream(args)
            .map(arg -> (Component) Component.text(arg.toString()))
            .toList())
        .build();
  }
}
