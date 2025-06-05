package pw.rebux.parkourdisplay.core.state;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class PlayerParkourState {

  private int airTime = 0;
}
