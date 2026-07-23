package pw.rebux.parkourdisplay.core.run;

import java.util.Date;

public record RunFileInfo(
    String name,
    Type type,
    Date lastModified
) {
  public enum Type {
    ZortMod,
    ParkourDisplay
  }
}
