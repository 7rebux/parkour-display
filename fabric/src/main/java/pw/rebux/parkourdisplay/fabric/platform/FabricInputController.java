package pw.rebux.parkourdisplay.fabric.platform;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import pw.rebux.parkourdisplay.common.platform.InputController;

/// Fabric-backed [InputController], wrapping the vanilla movement [KeyBinding]s.
public final class FabricInputController implements InputController {

  private final GameOptions options;

  public FabricInputController(GameOptions options) {
    this.options = options;
  }

  private KeyBinding binding(MovementKey key) {
    return switch (key) {
      case FORWARD -> this.options.forwardKey;
      case BACK -> this.options.backKey;
      case LEFT -> this.options.leftKey;
      case RIGHT -> this.options.rightKey;
      case SPRINT -> this.options.sprintKey;
      case SNEAK -> this.options.sneakKey;
      case JUMP -> this.options.jumpKey;
    };
  }

  @Override
  public boolean isDown(MovementKey key) {
    return this.binding(key).isPressed();
  }

  @Override
  public void setPressed(MovementKey key, boolean pressed) {
    this.binding(key).setPressed(pressed);
  }
}
