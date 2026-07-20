package pw.rebux.parkourdisplay.core.macro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

/// Verifies {@link RecordingConverter} against the two format quirks a third-party movement
/// recording has compared to a native macro: `yaw` is only present on a row when it changes
/// (carried forward from the previous tick otherwise), and `pitch` may not be present per row at
/// all (carried forward from `start.pitch`/the previous tick).
class RecordingConverterTest {

  private static final Gson GSON = new Gson();

  private static JsonObject recording(String json) {
    return GSON.fromJson(json, JsonObject.class);
  }

  @Test
  void mapsKeyTokensAndUsesTheRowsOwnYawAndStartPitch() {
    var recording = recording("""
        {
          "start": { "yaw": 0.0, "pitch": 40.0 },
          "rows": [
            { "keys": ["W", "SPRINT", "JUMP"], "yaw": 40.23245 }
          ]
        }
        """);

    var result = RecordingConverter.convert(recording);

    assertEquals(1, result.size());

    var tick = result.get(0);
    assertTrue(tick.w());
    assertFalse(tick.a());
    assertFalse(tick.s());
    assertFalse(tick.d());
    assertTrue(tick.jump());
    assertTrue(tick.sprint());
    assertFalse(tick.sneak());
    assertEquals(40.23245f, tick.yaw());
    assertEquals(40.0f, tick.pitch());
  }

  @Test
  void carriesForwardYawAndPitchWhenARowOmitsThem() {
    var recording = recording("""
        {
          "start": { "yaw": 0.0, "pitch": 40.0 },
          "rows": [
            { "keys": ["W"], "yaw": 12.5, "pitch": 20.0 },
            { "keys": ["W"] }
          ]
        }
        """);

    var result = RecordingConverter.convert(recording);

    assertEquals(12.5f, result.get(1).yaw());
    assertEquals(20.0f, result.get(1).pitch());
  }

  @Test
  void fallsBackToStartYawWhenTheFirstRowOmitsIt() {
    var recording = recording("""
        {
          "start": { "yaw": 7.0, "pitch": 40.0 },
          "rows": [
            { "keys": [] }
          ]
        }
        """);

    var result = RecordingConverter.convert(recording);

    assertEquals(7.0f, result.get(0).yaw());
    assertEquals(40.0f, result.get(0).pitch());
  }
}
