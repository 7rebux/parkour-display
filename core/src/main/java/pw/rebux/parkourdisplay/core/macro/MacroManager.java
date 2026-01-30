package pw.rebux.parkourdisplay.core.macro;

import static net.labymod.api.client.component.Component.text;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@RequiredArgsConstructor
public class MacroManager {

  private static final File MACROS_DIR = new File(ParkourDisplayAddon.DATA_DIR, "macros");
  private static final Type GSON_DATA_TYPE = new TypeToken<ArrayList<MacroTickState>>() {}.getType();

  private final ParkourDisplayAddon addon;

  static {
    if (!MACROS_DIR.exists() && !MACROS_DIR.mkdirs()) {
      throw new RuntimeException("Failed to create macros directory: " + MACROS_DIR.getAbsolutePath());
    }
  }

  @Getter
  private final ArrayDeque<MacroTickState> activeMacro = new ArrayDeque<>();

  public List<MacroFile> listAvailableFiles() {
    var files = MACROS_DIR.listFiles(f -> f.getName().endsWith(".json"));
    return files == null
        ? List.of()
        : Arrays.stream(files)
            .map(f ->
                new MacroFile(
                    f.getName().replace(".json", ""),
                    f.lastModified()))
            .sorted(Comparator.comparing(MacroFile::lastModified))
            .toList();
  }

  public List<MacroTickState> loadMacro(String name) throws FileNotFoundException {
    var file = new File(MACROS_DIR, name + ".json");
    var reader = new JsonReader(new FileReader(file));

    return this.addon.gson().fromJson(reader, GSON_DATA_TYPE);
  }

  public void saveMacro(List<MacroTickState> tickInputs, String name) throws IOException {
    var file = new File(MACROS_DIR, name + ".json");

    try (FileWriter writer = new FileWriter(file)) {
      this.addon.gson().toJson(tickInputs, writer);
    }
  }

  public void runMacro(List<MacroTickState> tickInputs) {
    if (!this.isAllowed()) {
      this.addon.displayMessage(
          text("Macros are disabled on this server.", NamedTextColor.RED));
      return;
    }

    this.activeMacro.clear();
    this.activeMacro.addAll(tickInputs);
  }

  private boolean isAllowed() {
    return this.addon.labyAPI()
        .permissionRegistry()
        .isPermissionEnabled(ParkourDisplayAddon.MACRO_PERMISSION);
  }
}
