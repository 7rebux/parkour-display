package pw.rebux.parkourdisplay.core.state;

import lombok.Data;

@Data
public final class PlayerParkourState {

  // TODO: Move to Landing block manager?
  private double lastTotalLandingBlockOffset = 0;
  private double lastLandingBlockOffsetX = 0, lastLandingBlockOffsetZ = 0;
}
