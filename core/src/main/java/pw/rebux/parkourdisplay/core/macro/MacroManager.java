package pw.rebux.parkourdisplay.core.macro;

import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class MacroManager {

  private final ParkourDisplayAddon addon;
  private final File macrosDirectory;

  @Getter
  private final Map<String, Macro> macros = new HashMap<>();

  public MacroManager(ParkourDisplayAddon addon) {
    this.addon = addon;
    this.macrosDirectory = new File(addon.addonInfo().getDirectoryPath().toFile(), "macros");

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
        Macro macro = this.addon.gson().fromJson(reader, ArrayList.class);

        this.macros.put(file.getName().replace("json", ""), macro);
      }

      this.addon.logger().info("Loaded %d macros.".formatted(this.macros.size()));
    } catch (Exception e) {
      this.addon.logger().error("Failed to load macros.", e);
    }
  }
}
