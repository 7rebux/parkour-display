package pw.rebux.parkourdisplay.core.run;

import pw.rebux.parkourdisplay.core.util.TickPosition;

/// Holds information for a single tick in a run.
///
/// @param input    The users' keyboard and mouse input data for this tick.
/// @param position Information about the players' position for this tick.
/// @param airTime  The air time of the tick, i.e. its index within the airborne stretch.
/// @param vx       The X movement applied in this tick.
/// @param vy       The Y movement applied in this tick.
/// @param vz       The Z movement applied in this tick.
public record RunTickState(
    KeyboardInput input,
    TickPosition position,
    long airTime,
    double vx,
    double vy,
    double vz
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

    public String formatted() {
      var input = new StringBuilder();

      if (this.w) input.append("W");
      if (this.a) input.append("A");
      if (this.s) input.append("S");
      if (this.d) input.append("D");
      if (this.jump) input.append(input.isEmpty() ? "" : " ").append("Jump");
      if (this.sneak) input.append(input.isEmpty() ? "" : " ").append("Sneak");
      if (this.sprint) input.append(input.isEmpty() ? "" : " ").append("Sprint");

      return input.isEmpty() ? "-" : input.toString();
    }
  }
}
