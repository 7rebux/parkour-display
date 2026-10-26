package pw.rebux.parkourdisplay.core.tickdot;

import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

/// The state of a single tick that is rendered as a dot in the world, captured once its movement
/// was applied.
///
/// @param airTime The air time of the tick, i.e. its index within the airborne stretch.
/// @param hitbox  The player's bounding box, shown instead of the dot once it is toggled.
public record TickDot(
    long airTime,
    double x, double y, double z,
    double vx, double vy, double vz,
    float yaw,
    AxisAlignedBoundingBox hitbox,
    boolean forward, boolean left, boolean back, boolean right,
    boolean jump, boolean sneak, boolean sprinting,
    boolean onGround, boolean onClimbable, boolean collided
) {

  /// Captures the current tick. Must be called after the player state was updated for this tick.
  public static TickDot capture(ParkourDisplayAddon addon, ClientPlayer player) {
    var state = addon.playerState();
    var currentTick = state.currentTick();
    var inputUtil = addon.minecraftInputUtil();

    return new TickDot(
        state.airTime(),
        currentTick.x(), currentTick.y(), currentTick.z(),
        state.vx(), state.vy(), state.vz(),
        currentTick.yaw(),
        currentTick.playerBoundingBox(),
        inputUtil.forwardKey().isDown(),
        inputUtil.leftKey().isDown(),
        inputUtil.backKey().isDown(),
        inputUtil.rightKey().isDown(),
        inputUtil.jumpKey().isDown(),
        inputUtil.sneakKey().isDown(),
        player.isSprinting(),
        currentTick.onGround(),
        currentTick.onClimbable(),
        currentTick.collided()
    );
  }

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
