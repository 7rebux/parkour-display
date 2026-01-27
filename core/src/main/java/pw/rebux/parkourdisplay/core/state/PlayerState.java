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

  /**
   * Amount of ticks the player has been continuously in the air.
   */
  private long airTicks;
  /**
   * Amount of ticks the player has been continuously on the ground.
   */
  private long groundTicks;
}
