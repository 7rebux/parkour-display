package pw.rebux.parkourdisplay.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestTickPositions {

  public static TickPosition of(double y, double vy, boolean onGround, boolean onClimbable) {
    return TickPosition.builder()
        .y(y)
        .vy(vy)
        .onGround(onGround)
        .onClimbable(onClimbable)
        .build();
  }
}
