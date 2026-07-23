package pw.rebux.parkourdisplay.fabric.platform;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import pw.rebux.parkourdisplay.common.platform.ChatOutput;
import pw.rebux.parkourdisplay.common.platform.Message;
import pw.rebux.parkourdisplay.common.platform.TextColorType;

/// Fabric-backed [ChatOutput]. Builds a translatable [Text] using the same keys (shipped in the
/// mod's `lang/en_us.json`) and `[PD]` prefix as the LabyMod addon, so output stays equivalent.
public final class FabricChatOutput implements ChatOutput {

  private static final String KEY_PREFIX = "parkourdisplay.";

  private final MinecraftClient client;

  public FabricChatOutput(MinecraftClient client) {
    this.client = client;
  }

  @Override
  public void display(Message message) {
    var args = message.args().stream()
        .map(arg -> arg.color() == null
            ? (Object) arg.value()
            : Text.literal(arg.value()).formatted(map(arg.color())))
        .toArray();

    MutableText body = Text.translatable(KEY_PREFIX + message.key(), args);
    if (message.color() != null) {
      body.formatted(map(message.color()));
    }

    var out = message.prefix() ? prefix().append(body) : body;

    if (this.client.player != null) {
      this.client.player.sendMessage(out, false);
    } else if (this.client.inGameHud != null) {
      this.client.inGameHud.getChatHud().addMessage(out);
    }
  }

  private static MutableText prefix() {
    return Text.literal("[").formatted(Formatting.DARK_AQUA)
        .append(Text.literal("PD").formatted(Formatting.AQUA))
        .append(Text.literal("]").formatted(Formatting.DARK_AQUA))
        .append(Text.literal(" "));
  }

  private static Formatting map(TextColorType color) {
    return switch (color) {
      case GRAY -> Formatting.GRAY;
      case RED -> Formatting.RED;
      case GREEN -> Formatting.GREEN;
      case DARK_GREEN -> Formatting.DARK_GREEN;
      case DARK_RED -> Formatting.DARK_RED;
      case AQUA -> Formatting.AQUA;
      case DARK_AQUA -> Formatting.DARK_AQUA;
    };
  }
}
