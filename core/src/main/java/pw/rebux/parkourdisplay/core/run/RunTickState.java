package pw.rebux.parkourdisplay.core.run;

import pw.rebux.parkourdisplay.core.tickdot.TickDot;
import pw.rebux.parkourdisplay.core.util.TickPosition;

/// Holds information for a single tick in a run.
///
/// @param input    The users' keyboard and mouse input data for this tick.
/// @param position Information about the players' position for this tick.
/// @param dot      The tick's state as shown when looking at its dot in the world.
public record RunTickState(
    KeyboardInput input,
    TickPosition position,
    TickDot dot
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
