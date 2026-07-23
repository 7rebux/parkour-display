package pw.rebux.parkourdisplay.common.platform;

import java.util.ArrayList;
import java.util.List;

/// A platform-neutral, translatable chat message: a translation key, an overall color, an optional
/// `[PD]` prefix, and ordered arguments (each optionally colored). Platforms render this into their
/// own translatable text component using the same translation keys, so output stays identical.
public final class Message {

  /// A single translation argument. `value` is already formatted to a string by the domain;
  /// `color` is optional (null keeps the surrounding message color).
  public record Arg(String value, TextColorType color) {
  }

  private final String key;
  private final List<Arg> args = new ArrayList<>();
  private TextColorType color;
  private boolean prefix = true;

  private Message(String key) {
    this.key = key;
  }

  public static Message of(String key) {
    return new Message(key);
  }

  public Message color(TextColorType color) {
    this.color = color;
    return this;
  }

  public Message noPrefix() {
    this.prefix = false;
    return this;
  }

  public Message arg(String value) {
    this.args.add(new Arg(value, null));
    return this;
  }

  public Message arg(String value, TextColorType color) {
    this.args.add(new Arg(value, color));
    return this;
  }

  public String key() {
    return this.key;
  }

  public List<Arg> args() {
    return this.args;
  }

  public TextColorType color() {
    return this.color;
  }

  public boolean prefix() {
    return this.prefix;
  }
}
