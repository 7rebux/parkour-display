package pw.rebux.parkourdisplay.core.graph;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

/// Writes rendered jump graphs to disk (mirrors {@code RunFileManager}).
public final class JumpGraphFileManager {

  private static final File GRAPHS_DIR = new File(ParkourDisplayAddon.DATA_DIR, "jumps");

  static {
    if (!GRAPHS_DIR.exists() && !GRAPHS_DIR.mkdirs()) {
      throw new RuntimeException(
          "Failed to create jump graphs directory: %s".formatted(GRAPHS_DIR.getAbsolutePath()));
    }
  }

  /// Renders the samples to a PNG under the jumps directory and returns the written file.
  public File save(List<JumpSample> samples, String name) throws IOException {
    var image = JumpGraphRenderer.render(samples);
    var file = new File(GRAPHS_DIR, name + ".png");
    ImageIO.write(image, "png", file);
    return file;
  }
}
