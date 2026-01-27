package pw.rebux.parkourdisplay.core.state;

import lombok.Data;

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

  private long airTicks;

  public double vx() {
    return this.currentTick.x() - this.lastTick.x();
  }

  public double vy() {
    return this.currentTick.y() - this.lastTick.y();
  }

  public double vz() {
    return this.currentTick.z() - this.lastTick.z();
  }
}
