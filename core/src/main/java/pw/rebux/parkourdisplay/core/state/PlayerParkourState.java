package pw.rebux.parkourdisplay.core.state;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PlayerParkourState {

  private int airTime = 0;
  private double jumpX = 0, jumpY = 0, jumpZ = 0;
  private double jumpYaw = 0, jumpPitch = 0;
  private double landingX = 0, landingY = 0, landingZ = 0;
  private double hitX = 0, hitY = 0, hitZ = 0;
}
