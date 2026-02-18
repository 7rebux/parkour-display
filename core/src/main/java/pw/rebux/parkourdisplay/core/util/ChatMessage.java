package pw.rebux.parkourdisplay.core.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import net.labymod.api.Laby;
import net.labymod.api.client.chat.ChatExecutor;
import net.labymod.api.client.chat.command.Command;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.util.I18n;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class ChatMessage {

  private static final Component messagePrefix =
      Component.translatable("%s.prefix".formatted(ParkourDisplayAddon.NAMESPACE));
  private static final ChatExecutor chatExecutor = Laby.labyAPI().minecraft().chatExecutor();

  // Argument transformers
  private static final SimpleDateFormat dateFormat =
      new SimpleDateFormat("yyyy-MM-dd HH:mm");

  private final String message;
  private final String translationKey;
  private Object[] args = new Object[] { };
  private TextColor textColor;
  private boolean withPrefix = true;

  public ChatMessage(String message, String translationKey) {
    this.message = message;
    this.translationKey = translationKey;
  }

  public static ChatMessage of(String message) {
    return new ChatMessage(message, null);
  }

  public static ChatMessage ofTranslatable(String key) {
    return new ChatMessage(null, key);
  }

  public ChatMessage withArgs(Object... args) {
    this.args = args;
    return this;
  }

  public ChatMessage withPrefix(boolean withPrefix) {
    this.withPrefix = withPrefix;
    return this;
  }

  public ChatMessage withColor(TextColor textColor) {
    this.textColor = textColor;
    return this;
  }

  public void send() {
    Component base = withPrefix ? messagePrefix.append(Component.space()) : Component.empty();
    Component message = translationKey != null
        ? Component.text(processTranslatable(translationKey, args))
        : Component.text(this.message);

    chatExecutor.displayClientMessage(base.append(message.color(textColor)));
  }

  public static String commandKey(Command command, String key) {
    return "commands.%s.messages.%s".formatted(command.getPrefix(), key);
  }

  private String processTranslatable(String key, Object... args) {
    var transformedArgs = Arrays.stream(args)
        .map(object -> object instanceof Date ? dateFormat.format(object) : object)
        .toArray();

    return I18n.translate("%s.%s".formatted(ParkourDisplayAddon.NAMESPACE, key), transformedArgs);
  }
}
