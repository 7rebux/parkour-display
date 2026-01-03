package pw.rebux.parkourdisplay.core.splits;

public record SplitFile(
    String name,
    String type,
    long lastModified
) {
}
