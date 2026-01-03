package pw.rebux.parkourdisplay.core;

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
import pw.rebux.parkourdisplay.core.state.PositionOffset;
import pw.rebux.parkourdisplay.core.state.RunSplit;

@RequiredArgsConstructor
public final class SplitsManager {

  private static final File SPLITS_DIR = new File(ParkourDisplayAddon.DATA_DIR, "splits");
  private static final File ZORTMOD_DATA_DIR = new File("zmdata");

  private final ParkourDisplayAddon addon;

  static {
    if (!SPLITS_DIR.exists() && !SPLITS_DIR.mkdir()) {
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
    var rootObj = new JsonObject();

    var startPos = this.addon.playerParkourState().runStartPosition();
    var startPosObj = new JsonObject();
    startPosObj.addProperty("x", startPos.posX());
    startPosObj.addProperty("y", startPos.posY());
    startPosObj.addProperty("z", startPos.posZ());
    rootObj.add("startPos", startPosObj);
    rootObj.add("startDx", new JsonPrimitive(startPos.offsetX()));
    rootObj.add("startDz", new JsonPrimitive(startPos.offsetZ()));

    var endPos = this.addon.playerParkourState().runEndPosition();
    var endPosObj = new JsonObject();
    endPosObj.addProperty("x", endPos.posX());
    endPosObj.addProperty("y", endPos.posY());
    endPosObj.addProperty("z", endPos.posZ());
    rootObj.add("endPos", endPosObj);
    rootObj.add("endDx", new JsonPrimitive(endPos.offsetX()));
    rootObj.add("endDz", new JsonPrimitive(endPos.offsetZ()));

    // TODO: pb: totalTicks

    var splits = this.addon.playerParkourState().runSplits();
    var splitsArrayObj = new JsonArray();
    var splitDxArrayObj = new JsonArray();
    var splitDzArrayObj = new JsonArray();
    var pbSplitsArrayObj = new JsonArray();
    // TODO: Ok this is what we have right now, best split times. Instead pb splits is best split time with finish
    var bestSplitsArrayObj = new JsonArray();
    // This is unused in zortmod, persisting an empty array to remain compatibility
    var goldSplitsArrayObj = new JsonArray();
    for (var split : splits) {
      var splitObj = new JsonObject();
      splitObj.addProperty("x", split.positionOffset().posX());
      splitObj.addProperty("y", split.positionOffset().posY());
      splitObj.addProperty("z", split.positionOffset().posZ());
      splitsArrayObj.add(splitObj);

      splitDxArrayObj.add(new JsonPrimitive(split.positionOffset().offsetX()));
      splitDzArrayObj.add(new JsonPrimitive(split.positionOffset().offsetZ()));
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

  // TODO: Handle pb
  public void loadSplits(String fileName) throws FileNotFoundException {
    var file = Optional.of(new File(SPLITS_DIR, fileName + ".json"))
        .filter(File::exists)
        .orElse(new File(ZORTMOD_DATA_DIR, fileName + ".json"));
    var rootObj = this.addon.gson().fromJson(new FileReader(file), JsonObject.class);

    var startPosObj = rootObj.getAsJsonObject("startPos");
    this.addon.playerParkourState().runStartPosition(
        PositionOffset.builder()
            .posX(startPosObj.get("x").getAsDouble())
            .posY(startPosObj.get("y").getAsDouble())
            .posZ(startPosObj.get("z").getAsDouble())
            .offsetX(rootObj.get("startDx").getAsDouble())
            .offsetZ(rootObj.get("startDz").getAsDouble())
            .build());

    var endPosObj = rootObj.getAsJsonObject("endPos");
    this.addon.playerParkourState().runEndPosition(
        PositionOffset.builder()
            .posX(endPosObj.get("x").getAsDouble())
            .posY(endPosObj.get("y").getAsDouble())
            .posZ(endPosObj.get("z").getAsDouble())
            .offsetX(rootObj.get("endDx").getAsDouble())
            .offsetZ(rootObj.get("endDz").getAsDouble())
            .build());

    var splitsArrayObj = rootObj.getAsJsonArray("splits");
    var splitDxArrayObj = rootObj.getAsJsonArray("splitDx");
    var splitDzArrayObj = rootObj.getAsJsonArray("splitDz");
    var pbSplitsArrayObj = rootObj.getAsJsonArray("pbSplits");

    this.addon.playerParkourState().runSplits().clear();

    for (var i = 0; i < rootObj.get("splitCount").getAsInt(); i++) {
      var splitObj = splitsArrayObj.get(i).getAsJsonObject();
      var split = new RunSplit(
          PositionOffset.builder()
              .posX(splitObj.get("x").getAsDouble())
              .posY(splitObj.get("y").getAsDouble())
              .posZ(splitObj.get("z").getAsDouble())
              .offsetX(splitDxArrayObj.get(i).getAsDouble())
              .offsetZ(splitDzArrayObj.get(i).getAsDouble())
              .build());
      split.personalBest(pbSplitsArrayObj.get(i).getAsInt());

      this.addon.playerParkourState().runSplits().add(split);
    }
  }

  private Stream<SplitFile> listJsonFiles(File dir, String source) {
    var files = dir.listFiles(f -> f.getName().endsWith(".json"));

    return files == null
        ? Stream.empty()
        : Arrays.stream(files).map(f -> new SplitFile(f.getName().split(".json")[0], source, f.lastModified()));
  }

  public record SplitFile(String fileName, String type, long lastModified) { }

}
