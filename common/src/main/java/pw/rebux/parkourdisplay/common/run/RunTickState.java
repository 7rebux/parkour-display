package pw.rebux.parkourdisplay.common.run;

import pw.rebux.parkourdisplay.common.state.PlayerSnapshot;

/// Holds information for a single tick in a run.
///
/// @param input    The users' keyboard and mouse input data for this tick.
/// @param position Information about the players' position for this tick.
public record RunTickState(
    KeyboardInput input,
    PlayerSnapshot position
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
