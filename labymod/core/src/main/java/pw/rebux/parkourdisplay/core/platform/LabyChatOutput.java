package pw.rebux.parkourdisplay.core.platform;

import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextColor;
import pw.rebux.parkourdisplay.common.platform.ChatOutput;
import pw.rebux.parkourdisplay.common.platform.Message;
import pw.rebux.parkourdisplay.common.platform.TextColorType;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

/// LabyMod-backed [ChatOutput]. Rebuilds each neutral [Message] through [ChatMessage] so the on
/// screen output is identical to the addon's previous direct usage.
public final class LabyChatOutput implements ChatOutput {

  @Override
  public void display(Message message) {
    var chatMessage = ChatMessage.of(message.key());

    if (!message.prefix()) {
      chatMessage.prefix(false);
    }

    var args = message.args().stream()
        .map(arg -> arg.color() == null
            ? (Object) arg.value()
            : Component.text(arg.value(), map(arg.color())))
        .toArray();
    chatMessage.withArgs(args);

    if (message.color() != null) {
      chatMessage.withColor(map(message.color()));
    }

    chatMessage.send();
  }

  static TextColor map(TextColorType color) {
    return switch (color) {
      case GRAY -> NamedTextColor.GRAY;
      case RED -> NamedTextColor.RED;
      case GREEN -> NamedTextColor.GREEN;
      case DARK_GREEN -> NamedTextColor.DARK_GREEN;
      case DARK_RED -> NamedTextColor.DARK_RED;
      case AQUA -> NamedTextColor.AQUA;
      case DARK_AQUA -> NamedTextColor.DARK_AQUA;
    };
  }
}
