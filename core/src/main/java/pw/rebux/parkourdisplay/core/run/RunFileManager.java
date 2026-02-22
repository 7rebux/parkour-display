package pw.rebux.parkourdisplay.core.run;

import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.run.RunFileInfo.Type;
import pw.rebux.parkourdisplay.core.run.split.Split;
import pw.rebux.parkourdisplay.core.run.split.SplitBoxTriggerMode;

@RequiredArgsConstructor
public final class RunFileManager {

  private static final File EXPORTS_DIR = new File(ParkourDisplayAddon.DATA_DIR, "splits");
  private static final File ZORTMOD_DATA_DIR = new File("zmdata");

  private final ParkourDisplayAddon addon;

  static {
    if (!EXPORTS_DIR.exists() && !EXPORTS_DIR.mkdirs()) {
      throw new RuntimeException(
          "Failed to create run exports directory: %s".formatted(EXPORTS_DIR.getAbsolutePath()));
    }
  }

  public void save(RunState run, String name) throws IOException {
    var file = new File(EXPORTS_DIR, name + ".json");
    var export = new RunExport(
        run.startPosition(),
        run.endSplit(),
        run.splits(),
        this.addon.landingBlockRegistry().landingBlocks()
    );

    try (FileWriter writer = new FileWriter(file)) {
      this.addon.gson().toJson(export, writer);
    }
  }

  public RunExport load(String fileName) throws FileNotFoundException {
    var file = new File(EXPORTS_DIR, fileName + ".json");
    return this.addon.gson().fromJson(new FileReader(file), RunExport.class);
  }

  public RunExport loadFromZM(String fileName) throws FileNotFoundException {
    var file = new File(ZORTMOD_DATA_DIR, fileName + ".json");
    var rootObj = this.addon.gson().fromJson(new FileReader(file), JsonObject.class);

    var startPosObj = rootObj.getAsJsonObject("startPos");
    var startPosition = new DoubleVector3(
        startPosObj.get("x").getAsDouble(),
        startPosObj.get("y").getAsDouble(),
        startPosObj.get("z").getAsDouble()
    );

    var endPosObj = rootObj.getAsJsonObject("endPos");
    var runEndSplit = new Split(
        "Finish",
        new AxisAlignedBoundingBox(
            endPosObj.get("x").getAsDouble() - (rootObj.get("endDx").getAsDouble() / 2),
            endPosObj.get("y").getAsDouble(),
            endPosObj.get("z").getAsDouble() - (rootObj.get("endDz").getAsDouble() / 2),
            endPosObj.get("x").getAsDouble() + (rootObj.get("endDx").getAsDouble() / 2),
            endPosObj.get("y").getAsDouble(),
            endPosObj.get("z").getAsDouble() + (rootObj.get("endDz").getAsDouble() / 2)),
        SplitBoxTriggerMode.IntersectXZSameY);
    runEndSplit.personalBest(rootObj.get("pb").getAsLong());

    var splitsArrayObj = rootObj.getAsJsonArray("splits");
    var splitDxArrayObj = rootObj.getAsJsonArray("splitDx");
    var splitDzArrayObj = rootObj.getAsJsonArray("splitDz");
    var bestSplitsArrayObj = rootObj.getAsJsonArray("bestSplits");

    var splits = new ArrayList<Split>();
    for (var i = 0; i < rootObj.get("splitCount").getAsInt(); i++) {
      var splitObj = splitsArrayObj.get(i).getAsJsonObject();
      var split = new Split(
          "Split %d".formatted(i + 1),
          new AxisAlignedBoundingBox(
              splitObj.get("x").getAsDouble() - (splitDxArrayObj.get(i).getAsDouble() / 2),
              splitObj.get("y").getAsDouble(),
              splitObj.get("z").getAsDouble() - (splitDzArrayObj.get(i).getAsDouble() / 2),
              splitObj.get("x").getAsDouble() + (splitDxArrayObj.get(i).getAsDouble() / 2),
              splitObj.get("y").getAsDouble(),
              splitObj.get("z").getAsDouble() + (splitDzArrayObj.get(i).getAsDouble() / 2)),
          SplitBoxTriggerMode.IntersectXZSameY);
      split.personalBest(bestSplitsArrayObj.get(i).getAsLong());

      splits.add(split);
    }

    return new RunExport(
        startPosition,
        runEndSplit,
        splits,
        Collections.emptyList()
    );
  }

  public List<RunFileInfo> availableFiles() {
    return Stream
        .concat(
            listJsonFiles(EXPORTS_DIR, Type.ParkourDisplay),
            listJsonFiles(ZORTMOD_DATA_DIR, Type.ZortMod))
        .sorted(Comparator.comparing(RunFileInfo::lastModified))
        .toList();
  }

  private Stream<RunFileInfo> listJsonFiles(File dir, Type type) {
    var files = dir.listFiles(f -> f.getName().endsWith(".json"));
    return files == null
        ? Stream.empty()
        : Arrays.stream(files).map(f ->
            new RunFileInfo(
                f.getName().split(".json")[0],
                type,
                new Date((f.lastModified()))));
  }
}
