package pw.rebux.parkourdisplay.core.offset;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

/// Announces the result of a single attempt at a target and maintains its personal best.
@RequiredArgsConstructor
public final class OffsetReporter {

  private final ParkourDisplayAddon addon;

  /// @param keyPrefix   translation key prefix, e.g. `messages.lb`, expected to resolve both
  ///                    `<prefix>.newPB` and `<prefix>.offsets`.
  /// @param distance    signed margin, where a positive value means the target was hit.
  /// @param showOffsets whether attempts that do not beat the personal best are announced.
  /// @param offsets     per-axis offsets in message argument order.
  /// @return true if this attempt became the new personal best.
  public boolean report(
      String keyPrefix,
      PersonalBest best,
      double distance,
      boolean showOffsets,
      double... offsets
  ) {
    var format = this.addon.configuration().decimalFormat();
    var hit = distance > 0;

    // A new personal best is always announced, regardless of `showOffsets`.
    if (best.submit(distance)) {
      var args = new Object[offsets.length + 1];
      args[0] = Component.text(String.format(format, distance), NamedTextColor.DARK_GREEN);

      for (int i = 0; i < offsets.length; i++) {
        args[i + 1] = String.format(format, offsets[i]);
      }

      ChatMessage.of("%s.newPB".formatted(keyPrefix))
          .withColor(NamedTextColor.GREEN)
          .withArgs(args)
          .send();

      return true;
    }

    if (showOffsets) {
      var color = hit ? NamedTextColor.DARK_GREEN : NamedTextColor.DARK_RED;
      var args = new Object[offsets.length];

      for (int i = 0; i < offsets.length; i++) {
        args[i] = Component.text(String.format(format, offsets[i]), color);
      }

      ChatMessage.of("%s.offsets".formatted(keyPrefix))
          .withColor(hit ? NamedTextColor.GREEN : NamedTextColor.RED)
          .withArgs(args)
          .send();
    }

    return false;
  }
}
