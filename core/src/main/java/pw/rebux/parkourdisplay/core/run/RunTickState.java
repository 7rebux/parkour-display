package pw.rebux.parkourdisplay.core.run;

import net.labymod.api.util.math.AxisAlignedBoundingBox;
import pw.rebux.parkourdisplay.core.util.TickPosition;

/// Holds information for a single tick in a run.
///
/// @param input    The users' keyboard and mouse input data for this tick.
/// @param position Information about the players' position for this tick.
/// @param playerBB The players' absolute bounding box for this tick.
public record RunTickState(
    KeyboardInput input,
    TickPosition position,
    // We could work with a constant bounding box for the player, but there are states in which
    // the size can change, e.g., when the player is crouching or swimming.
    AxisAlignedBoundingBox playerBB
) {

  public record KeyboardInput(
      boolean w,
      boolean a,
      boolean s,
      boolean d,
      boolean jump,
      boolean sprint,
      boolean sneak
  ) {
  }
}
