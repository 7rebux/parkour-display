package pw.rebux.parkourdisplay.common.persist;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import pw.rebux.parkourdisplay.common.macro.MacroTickState;

/// Reads and writes macros as JSON under `<dataDir>/macros`. Platform-neutral: the caller supplies
/// the base directory and a Gson configured with [MacroTickStateTypeAdapter].
public final class MacroRepository {

  private static final Type GSON_DATA_TYPE =
      new TypeToken<ArrayList<MacroTickState>>() {}.getType();

  private final File macrosDir;
  private final Gson gson;

  public MacroRepository(Path baseDir, Gson gson) {
    this.macrosDir = baseDir.resolve("macros").toFile();
    this.gson = gson;

    if (!this.macrosDir.exists() && !this.macrosDir.mkdirs()) {
      throw new RuntimeException(
          "Failed to create macros directory: %s".formatted(this.macrosDir.getAbsolutePath()));
    }
  }

  public List<MacroTickState> load(String name) throws FileNotFoundException {
    var file = new File(this.macrosDir, name + ".json");
    var reader = new JsonReader(new FileReader(file));
    return this.gson.fromJson(reader, GSON_DATA_TYPE);
  }

  public void save(List<MacroTickState> inputs, String name) throws IOException {
    var file = new File(this.macrosDir, name + ".json");
    try (FileWriter writer = new FileWriter(file)) {
      this.gson.toJson(inputs, writer);
    }
  }

  public List<MacroFileInfo> availableFiles() {
    var files = this.macrosDir.listFiles(f -> f.getName().endsWith(".json"));

    if (files == null) {
      return List.of();
    }

    var result = new ArrayList<MacroFileInfo>(files.length);

    for (var f : files) {
      result.add(
          new MacroFileInfo(
              f.getName().replace(".json", ""),
              new Date(f.lastModified())
          )
      );
    }

    result.sort(Comparator.comparing(MacroFileInfo::lastModified));

    return result;
  }
}
