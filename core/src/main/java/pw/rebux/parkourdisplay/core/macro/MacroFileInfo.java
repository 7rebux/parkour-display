package pw.rebux.parkourdisplay.core.macro;

import java.util.Date;

public record MacroFileInfo(
    String name,
    Type type,
    Date lastModified
) {
  public enum Type {
    Native,
    Recording
  }
}
