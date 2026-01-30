package pw.rebux.parkourdisplay.core.macro;

public record TickInput(
  boolean w,
  boolean a,
  boolean s,
  boolean d,
  boolean jump,
  boolean sprint,
  boolean sneak
) {
}
