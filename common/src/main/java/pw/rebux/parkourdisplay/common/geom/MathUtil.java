package pw.rebux.parkourdisplay.common.geom;

/// Small numeric helpers ported from LabyMod's `MathHelper` (only the pieces the run/macro
/// logic uses), kept platform-neutral so the domain no longer depends on the game math library.
public final class MathUtil {

  private MathUtil() {
  }

  public static float clamp(float value, float min, float max) {
    if (value < min) {
      return min;
    }
    return Math.min(value, max);
  }

  /// Wraps an angle into the range [-180, 180), matching LabyMod's `MathHelper.wrapDegrees`.
  public static float wrapDegrees(float value) {
    var wrapped = value % 360.0F;
    if (wrapped >= 180.0F) {
      wrapped -= 360.0F;
    }
    if (wrapped < -180.0F) {
      wrapped += 360.0F;
    }
    return wrapped;
  }
}
