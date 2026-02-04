package pw.rebux.parkourdisplay.core.run;

public record RunFileInfo(
    String name,
    Type type,
    long lastModified
) {
  public enum Type {
    ZortMod,
    ParkourDisplay
  }
}
