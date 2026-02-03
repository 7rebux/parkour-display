package pw.rebux.parkourdisplay.core.run.split;

public record SplitFile(
    String name,
    String type,
    long lastModified
) {

  // TODO
  public enum Type {
    ZortMod,
    ParkourDisplay
  }
}
