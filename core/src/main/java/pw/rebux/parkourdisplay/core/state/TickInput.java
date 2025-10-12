package pw.rebux.parkourdisplay.core.state;

public record TickInput(
  boolean w,
  boolean a,
  boolean s,
  boolean d,
  boolean jump,
  boolean sprint,
  boolean sneak,
  float yaw,
  float pitch
) {
}
