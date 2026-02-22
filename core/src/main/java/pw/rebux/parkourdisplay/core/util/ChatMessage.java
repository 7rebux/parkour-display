package pw.rebux.parkourdisplay.core.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import net.labymod.api.Laby;
import net.labymod.api.client.chat.ChatExecutor;
import net.labymod.api.client.chat.command.Command;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.util.I18n;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@RequiredArgsConstructor
public class ChatMessage {

  private static final ChatExecutor chatExecutor = Laby.labyAPI().minecraft().chatExecutor();
  private static final String commandMessageKey = "commands.%s.messages.%s";
  private static final Component messagePrefix =
      Component.translatable("%s.prefix".formatted(ParkourDisplayAddon.NAMESPACE));

  // Argument transformers
  private static final SimpleDateFormat dateFormat =
      new SimpleDateFormat("yyyy-MM-dd HH:mm");

  private final String translationKey;

  private boolean prefix = true;
  private Object[] args = new Object[] { };
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
    this.args = args;
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
    var message = Component.text(processTranslatable(translationKey, args));

    chatExecutor.displayClientMessage(base.append(message.color(textColor)));
  }

  private String processTranslatable(String key, Object... args) {
    var transformedArgs = Arrays.stream(args)
        .map(object -> object instanceof Date ? dateFormat.format(object) : object)
        .toArray();

    return I18n.translate("%s.%s".formatted(ParkourDisplayAddon.NAMESPACE, key), transformedArgs);
  }
}
