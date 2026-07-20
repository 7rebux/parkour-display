package pw.rebux.parkourdisplay.core.macro;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@RequiredArgsConstructor
public final class MacroFileManager {

  private static final File MACROS_DIR =
      new File(ParkourDisplayAddon.DATA_DIR, "macros");
  private static final File RECORDINGS_DIR =
      new File(ParkourDisplayAddon.DATA_DIR, "recordings");
  private static final Type GSON_DATA_TYPE =
      new TypeToken<ArrayList<MacroTickState>>() {}.getType();

  private final ParkourDisplayAddon addon;

  static {
    if (!MACROS_DIR.exists() && !MACROS_DIR.mkdirs()) {
      throw new RuntimeException(
          "Failed to create macros directory: %s".formatted(MACROS_DIR.getAbsolutePath()));
    }
    if (!RECORDINGS_DIR.exists() && !RECORDINGS_DIR.mkdirs()) {
      throw new RuntimeException(
          "Failed to create recordings directory: %s".formatted(
              RECORDINGS_DIR.getAbsolutePath()));
    }
  }

  /// Loads a macro by name, checking the native {@code macros} folder first and falling back to
  /// the {@code recordings} folder, converting on the fly via {@link RecordingConverter} if
  /// that's where it's found. See {@link RecordingConverter} for the format differences.
  public List<MacroTickState> load(String name) throws FileNotFoundException {
    var nativeFile = new File(MACROS_DIR, name + ".json");

    if (nativeFile.exists()) {
      var reader = new JsonReader(new FileReader(nativeFile));
      return this.addon.gson().fromJson(reader, GSON_DATA_TYPE);
    }

    var recordingFile = new File(RECORDINGS_DIR, name + ".json");
    var recording = this.addon.gson().fromJson(new FileReader(recordingFile), JsonObject.class);
    return RecordingConverter.convert(recording);
  }

  public void save(List<MacroTickState> inputs, String name) throws IOException {
    var file = new File(MACROS_DIR, name + ".json");
    try (FileWriter writer = new FileWriter(file)) {
      this.addon.gson().toJson(inputs, writer);
    }
  }

  public List<MacroFileInfo> availableFiles() {
    var result = new ArrayList<MacroFileInfo>();

    listJsonFiles(result, MACROS_DIR, MacroFileInfo.Type.Native);
    listJsonFiles(result, RECORDINGS_DIR, MacroFileInfo.Type.Recording);
    result.sort(Comparator.comparing(MacroFileInfo::lastModified));

    return result;
  }

  private void listJsonFiles(List<MacroFileInfo> result, File dir, MacroFileInfo.Type type) {
    var files = dir.listFiles(f -> f.getName().endsWith(".json"));

    if (files == null) {
      return;
    }

    for (var f : files) {
      result.add(
          new MacroFileInfo(
              f.getName().replace(".json", ""),
              type,
              new Date(f.lastModified())
          )
      );
    }
  }
}
