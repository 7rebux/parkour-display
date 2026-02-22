package pw.rebux.parkourdisplay.core.macro;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@RequiredArgsConstructor
public final class MacroFileManager {

  private static final File MACROS_DIR =
      new File(ParkourDisplayAddon.DATA_DIR, "macros");
  private static final Type GSON_DATA_TYPE =
      new TypeToken<ArrayList<MacroTickState>>() {}.getType();

  private final ParkourDisplayAddon addon;

  static {
    if (!MACROS_DIR.exists() && !MACROS_DIR.mkdirs()) {
      throw new RuntimeException(
          "Failed to create macros directory: %s".formatted(MACROS_DIR.getAbsolutePath()));
    }
  }

  public List<MacroTickState> load(String name) throws FileNotFoundException {
    var file = new File(MACROS_DIR, name + ".json");
    var reader = new JsonReader(new FileReader(file));
    return this.addon.gson().fromJson(reader, GSON_DATA_TYPE);
  }

  public void save(List<MacroTickState> inputs, String name) throws IOException {
    var file = new File(MACROS_DIR, name + ".json");
    try (FileWriter writer = new FileWriter(file)) {
      this.addon.gson().toJson(inputs, writer);
    }
  }

  public List<MacroFileInfo> availableFiles() {
    var files = MACROS_DIR.listFiles(f -> f.getName().endsWith(".json"));
    return files == null
        ? List.of()
        : Arrays.stream(files)
            .map(f ->
                new MacroFileInfo(
                    f.getName().replace(".json", ""),
                    new Date(f.lastModified())))
            .sorted(Comparator.comparing(MacroFileInfo::lastModified))
            .toList();
  }
}
