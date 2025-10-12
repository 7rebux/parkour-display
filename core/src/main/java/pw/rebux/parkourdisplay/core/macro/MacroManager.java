package pw.rebux.parkourdisplay.core.macro;

import com.google.gson.stream.JsonReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.Accessors;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.state.TickInput;

@Accessors(fluent = true)
public class MacroManager {

  private final ParkourDisplayAddon addon;
  private final File macrosDirectory;

  @Getter
  private final Map<String, List<TickInput>> macros = new HashMap<>();

  @Getter
  private final ArrayDeque<TickInput> activeMacro = new ArrayDeque<>();

  public MacroManager(ParkourDisplayAddon addon) {
    this.addon = addon;
    this.macrosDirectory = new File("temp_todo", "macros");

    loadMacros();
  }

  public void loadMacros() {
    this.macros.clear();

    try {
      if (!this.macrosDirectory.exists()) {
        this.macrosDirectory.mkdir();
      }

      File[] files = this.macrosDirectory.listFiles(file -> file.getName().endsWith(".json"));

      if (files == null) {
        return;
      }

      for (File file : files) {
        JsonReader reader = new JsonReader(new FileReader(file));
        ArrayList<TickInput> macro = this.addon.gson().fromJson(reader, ArrayList.class);

        this.macros.put(file.getName().replace(".json", ""), macro);
      }

      this.addon.logger().info("Loaded %d macros.".formatted(this.macros.size()));
    } catch (Exception e) {
      this.addon.logger().error("Failed to load macros.", e);
    }
  }

  public void saveMacro(List<TickInput> tickStates, String name) throws IOException {
    this.macros.put(name, tickStates);

    var writer = new BufferedWriter(new FileWriter(new File(this.macrosDirectory, name + ".json")));
    writer.write(this.addon.gson().toJson(tickStates));
    writer.close();
  }

  public void runMacro(List<TickInput> macro) {
    this.activeMacro.clear();
    this.activeMacro.addAll(macro);
  }
}
