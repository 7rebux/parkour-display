package pw.rebux.parkourdisplay.core.macro;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

/// Converts a third-party movement recording (e.g. an angle-solver export such as
/// `e115.json`) into the list of {@link MacroTickState} the addon's own macro format uses.
///
/// The recording format differs from a native macro in two ways: `yaw` is only present on a
/// row when it changes, and `pitch` isn't tracked per row at all since these recordings only
/// cover horizontal movement. Both are carried forward from the previous tick (starting from
/// `start.yaw`/`start.pitch`) whenever a row omits them.
public final class RecordingConverter {

  private RecordingConverter() {
  }

  public static List<MacroTickState> convert(JsonObject recording) {
    var start = recording.getAsJsonObject("start");
    var rows = recording.getAsJsonArray("rows");

    var result = new ArrayList<MacroTickState>(rows.size());
    var yaw = start.get("yaw").getAsFloat();
    var pitch = start.get("pitch").getAsFloat();

    for (var element : rows) {
      var row = element.getAsJsonObject();
      var keys = row.getAsJsonArray("keys");

      if (row.has("yaw")) {
        yaw = row.get("yaw").getAsFloat();
      }
      if (row.has("pitch")) {
        pitch = row.get("pitch").getAsFloat();
      }

      result.add(new MacroTickState(
          containsKey(keys, "W"),
          containsKey(keys, "A"),
          containsKey(keys, "S"),
          containsKey(keys, "D"),
          containsKey(keys, "JUMP"),
          containsKey(keys, "SPRINT"),
          containsKey(keys, "SNEAK"),
          yaw,
          pitch
      ));
    }

    return result;
  }

  private static boolean containsKey(JsonArray keys, String key) {
    for (var element : keys) {
      if (element.getAsString().equalsIgnoreCase(key)) {
        return true;
      }
    }

    return false;
  }
}
