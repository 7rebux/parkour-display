package pw.rebux.parkourdisplay.core.util;

public final class MathHelper {

  public static float formatYaw(float yaw) {
    float facing = yaw % 360;
    if (facing > 180) facing -= 360;
    else if (facing < -180) facing += 360;
    return facing;
  }
}
