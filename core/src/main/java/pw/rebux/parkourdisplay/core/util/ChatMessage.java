package pw.rebux.parkourdisplay.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import net.labymod.api.Laby;
import net.labymod.api.client.chat.ChatExecutor;
import net.labymod.api.client.chat.command.Command;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@RequiredArgsConstructor
public class ChatMessage {

  private static final ChatExecutor chatExecutor = Laby.labyAPI().minecraft().chatExecutor();
  private static final String commandMessageKey = "commands.%s.messages.%s";
  private static final Component messagePrefix = Component.text()
      .append(Component.text("[", NamedTextColor.DARK_AQUA))
      .append(Component.text("PD", NamedTextColor.AQUA))
      .append(Component.text("]", NamedTextColor.DARK_AQUA))
      .build();

  // Argument transformers
  private static final SimpleDateFormat dateFormat =
      new SimpleDateFormat("yyyy-MM-dd HH:mm");

  private final String translationKey;

  private boolean prefix = true;
  private Component[] args = new Component[] { };
  private TextColor textColor;

  public static ChatMessage of(String translationKey) {
    return new ChatMessage(translationKey);
  }

  public static ChatMessage of(Command command, String key) {
    var translationKey = commandMessageKey.formatted(command.getPrefix(), key);
    return new ChatMessage(translationKey);
  }

  public ChatMessage prefix(boolean prefix) {
    this.prefix = prefix;
    return this;
  }

  public ChatMessage withArgs(Object... args) {
    this.args = new Component[args.length];

    for (int i = 0; i < args.length; i++) {
      if (args[i] instanceof Component) {
        this.args[i] = (Component) args[i];
      } else if (args[i] instanceof Date) {
        this.args[i] = Component.text(dateFormat.format(args[i]));
      } else {
        this.args[i] = Component.text(args[i].toString());
      }
    }

    return this;
  }

  public ChatMessage withColor(TextColor textColor) {
    this.textColor = textColor;
    return this;
  }

  public void send() {
    var base = prefix
        ? Component.empty().append(messagePrefix).append(Component.space())
        : Component.empty();
    var message = Component.translatable()
        .key("%s.%s".formatted(ParkourDisplayAddon.NAMESPACE, translationKey))
        .arguments(args)
        .build();

    chatExecutor.displayClientMessage(base.append(message.color(textColor)));
  }
}
