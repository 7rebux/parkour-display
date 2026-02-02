package pw.rebux.parkourdisplay.core.run.split;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

// TODO: We have to fix exports to support min and max y
@RequiredArgsConstructor
public final class SplitManager {

  private static final File SPLITS_DIR = new File(ParkourDisplayAddon.DATA_DIR, "splits");
  private static final File ZORTMOD_DATA_DIR = new File("zmdata");

  private final ParkourDisplayAddon addon;

  static {
    if (!SPLITS_DIR.exists() && !SPLITS_DIR.mkdirs()) {
      throw new RuntimeException("Failed to create splits directory: " + SPLITS_DIR.getAbsolutePath());
    }
  }

  public List<SplitFile> listAvailableFiles() {
    return Stream
        .concat(
            listJsonFiles(SPLITS_DIR, "pd"),
            listJsonFiles(ZORTMOD_DATA_DIR, "zm"))
        .sorted(Comparator.comparing(SplitFile::lastModified))
        .toList();
  }

  public void saveCurrentSplits(String fileName) throws IOException {
    var runState = this.addon.runState();

    var rootObj = new JsonObject();

    var startPos = runState.startPosition();
    var startPosObj = new JsonObject();
    startPosObj.addProperty("x", startPos.getX());
    startPosObj.addProperty("y", startPos.getY());
    startPosObj.addProperty("z", startPos.getZ());
    rootObj.add("startPos", startPosObj);
    rootObj.add("startDx", new JsonPrimitive(0.1));
    rootObj.add("startDz", new JsonPrimitive(0.1));

    var endBox = runState.endSplit().boundingBox();
    var endPosObj = new JsonObject();
    endPosObj.addProperty("x", endBox.getCenter().getX());
    endPosObj.addProperty("y", endBox.getMinY());
    endPosObj.addProperty("z", endBox.getCenter().getZ());
    rootObj.add("endPos", endPosObj);
    rootObj.add("endDx", new JsonPrimitive(endBox.getXWidth()));
    rootObj.add("endDz", new JsonPrimitive(endBox.getZWidth()));
    rootObj.add("pb", new JsonPrimitive(runState.endSplit().personalBest()));

    var splits = runState.splits();
    var splitsArrayObj = new JsonArray();
    var splitDxArrayObj = new JsonArray();
    var splitDzArrayObj = new JsonArray();
    // In zortmod these are only updated if the run was finished
    var pbSplitsArrayObj = new JsonArray();
    // These are always updated
    var bestSplitsArrayObj = new JsonArray();
    // This is unused in zortmod, persisting an empty array to remain compatibility
    var goldSplitsArrayObj = new JsonArray();
    for (var split : splits) {
      var splitBox = split.boundingBox();
      var splitObj = new JsonObject();
      splitObj.addProperty("x", splitBox.getCenter().getX());
      splitObj.addProperty("y", splitBox.getMinY());
      splitObj.addProperty("z", splitBox.getCenter().getZ());
      splitsArrayObj.add(splitObj);

      splitDxArrayObj.add(new JsonPrimitive(splitBox.getXWidth()));
      splitDzArrayObj.add(new JsonPrimitive(splitBox.getZWidth()));
      pbSplitsArrayObj.add(new JsonPrimitive(split.personalBest()));
      bestSplitsArrayObj.add(new JsonPrimitive(split.personalBest()));
    }
    rootObj.add("splitCount", new JsonPrimitive(splits.size()));
    rootObj.add("splits", splitsArrayObj);
    rootObj.add("splitDx", splitDxArrayObj);
    rootObj.add("splitDz", splitDzArrayObj);
    rootObj.add("pbSplits", pbSplitsArrayObj);
    rootObj.add("bestSplits", bestSplitsArrayObj);
    rootObj.add("goldSplits", goldSplitsArrayObj);

    var file = new File(SPLITS_DIR, fileName + ".json");
    try (FileWriter writer = new FileWriter(file)) {
      this.addon.gson().toJson(rootObj, writer);
    }
  }

  public void loadSplits(String fileName) throws FileNotFoundException {
    var file = Optional.of(new File(SPLITS_DIR, fileName + ".json"))
        .filter(File::exists)
        .orElse(new File(ZORTMOD_DATA_DIR, fileName + ".json"));
    var rootObj = this.addon.gson().fromJson(new FileReader(file), JsonObject.class);

    var startPosObj = rootObj.getAsJsonObject("startPos");
    this.addon.runState().startPosition(
        new DoubleVector3(
            startPosObj.get("x").getAsDouble(),
            startPosObj.get("y").getAsDouble(),
            startPosObj.get("z").getAsDouble()
        ));

    var endPosObj = rootObj.getAsJsonObject("endPos");
    var runEndSplit = new RunSplit(
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
    this.addon.runState().endSplit(runEndSplit);

    var splitsArrayObj = rootObj.getAsJsonArray("splits");
    var splitDxArrayObj = rootObj.getAsJsonArray("splitDx");
    var splitDzArrayObj = rootObj.getAsJsonArray("splitDz");
    var bestSplitsArrayObj = rootObj.getAsJsonArray("bestSplits");

    this.addon.runState().splits().clear();

    for (var i = 0; i < rootObj.get("splitCount").getAsInt(); i++) {
      var splitObj = splitsArrayObj.get(i).getAsJsonObject();
      var split = new RunSplit(
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

      this.addon.runState().splits().add(split);
    }
  }

  private Stream<SplitFile> listJsonFiles(File dir, String source) {
    var files = dir.listFiles(f -> f.getName().endsWith(".json"));
    return files == null
        ? Stream.empty()
        : Arrays.stream(files).map(f ->
            new SplitFile(
                f.getName().split(".json")[0],
                source,
                f.lastModified()));
  }
}
