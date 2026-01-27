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
}
