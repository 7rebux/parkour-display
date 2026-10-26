package pw.rebux.parkourdisplay.core.ladderbox;

/// A single tick of a recorded ladder trajectory, captured once its movement was applied.
///
/// @param tick The air time of the tick, i.e. its index within the airborne stretch.
public record TrajectoryTick(
    long tick,
    double x, double y, double z,
    double vx, double vy, double vz,
    float yaw,
    boolean forward, boolean left, boolean back, boolean right,
    boolean jump, boolean sneak, boolean sprinting,
    boolean onGround, boolean onClimbable, boolean collided
) {

  public String inputs() {
    var input = new StringBuilder();

    if (this.forward) input.append("W");
    if (this.left) input.append("A");
    if (this.back) input.append("S");
    if (this.right) input.append("D");
    if (this.jump) input.append(input.isEmpty() ? "" : " ").append("Jump");
    if (this.sneak) input.append(input.isEmpty() ? "" : " ").append("Sneak");
    if (this.sprinting) input.append(input.isEmpty() ? "" : " ").append("Sprint");

    return input.isEmpty() ? "-" : input.toString();
  }
}
