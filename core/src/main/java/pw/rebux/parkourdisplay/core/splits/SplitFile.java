package pw.rebux.parkourdisplay.core.splits;

public record SplitFile(
    String fileName,
    String type,
    long lastModified
) {
}
