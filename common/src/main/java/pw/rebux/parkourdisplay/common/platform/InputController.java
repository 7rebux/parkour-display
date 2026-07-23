package pw.rebux.parkourdisplay.common.platform;

/// Reads and writes the seven movement key states. Reading powers run/timing tracking; writing
/// drives macro playback. Implementations wrap LabyMod `MinecraftInputMapping` / Fabric `KeyBinding`.
public interface InputController {

  enum MovementKey {
    FORWARD,
    BACK,
    LEFT,
    RIGHT,
    SPRINT,
    SNEAK,
    JUMP
  }

  boolean isDown(MovementKey key);

  void setPressed(MovementKey key, boolean pressed);

  default void unpressAll() {
    for (var key : MovementKey.values()) {
      this.setPressed(key, false);
    }
  }

  default boolean isMoving() {
    return this.isDown(MovementKey.FORWARD)
        || this.isDown(MovementKey.BACK)
        || this.isDown(MovementKey.LEFT)
        || this.isDown(MovementKey.RIGHT);
  }

  default boolean isMovingSideways() {
    return this.isDown(MovementKey.LEFT) ^ this.isDown(MovementKey.RIGHT);
  }
}
