package pw.rebux.parkourdisplay.core.state;

import lombok.Data;
import pw.rebux.parkourdisplay.core.util.TickPosition;

@Data
public final class PlayerState {

  /**
   * Position data computed at the start of the current tick.
   */
  private TickPosition currentTick = new TickPosition();
  /**
   * Position data computed at the end of the current tick.
   */
  private TickPosition lastTick = new TickPosition();
  /**
   * Amount of ticks the player has been continuously in the air.
   */
  private long airTime;
  /**
   * Amount of ticks the player has been continuously on the ground.
   */
  private long groundTime;

  public double vx() {
    return this.currentTick.x() - this.lastTick.x();
  }

  public double vy() {
    return this.currentTick.y() - this.lastTick.y();
  }

  public double vz() {
    return this.currentTick.z() - this.lastTick.z();
  }

  public double yawTurn() {
    return this.currentTick.yaw() - this.lastTick.yaw();
  }
}
