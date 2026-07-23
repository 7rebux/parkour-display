package pw.rebux.parkourdisplay.fabric.platform;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import pw.rebux.parkourdisplay.common.geom.Vec3;
import pw.rebux.parkourdisplay.common.run.RunSplit;
import pw.rebux.parkourdisplay.common.run.RunState;

/// Reads and writes runs as JSON under `<dataDir>/splits`. Uses the shared [RunSplit]/[Vec3] types,
/// so the split geometry serializes identically to the LabyMod addon (this Fabric build omits the
/// LabyMod-only landing blocks).
public final class FabricRunRepository {

  /// Serialized run shape. Field names match the LabyMod `RunExport` for `start`/`end`/`splits`.
  public record RunData(Vec3 start, RunSplit end, List<RunSplit> splits) {
  }

  private final File dir;
  private final Gson gson;

  public FabricRunRepository(java.nio.file.Path baseDir, Gson gson) {
    this.dir = baseDir.resolve("splits").toFile();
    this.gson = gson;

    if (!this.dir.exists() && !this.dir.mkdirs()) {
      throw new RuntimeException(
          "Failed to create splits directory: %s".formatted(this.dir.getAbsolutePath()));
    }
  }

  public void save(RunState run, String name) throws IOException {
    var data = new RunData(run.startPosition(), run.endSplit(), run.splits());
    try (FileWriter writer = new FileWriter(new File(this.dir, name + ".json"))) {
      this.gson.toJson(data, writer);
    }
  }

  public RunData load(String name) throws FileNotFoundException {
    return this.gson.fromJson(new FileReader(new File(this.dir, name + ".json")), RunData.class);
  }

  public List<String> availableFiles() {
    var files = this.dir.listFiles(f -> f.getName().endsWith(".json"));
    if (files == null) {
      return List.of();
    }
    return Arrays.stream(files)
        .sorted(Comparator.comparingLong(File::lastModified))
        .map(f -> f.getName().replace(".json", ""))
        .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
  }
}
