package pw.rebux.parkourdisplay.core.platform;

import net.labymod.api.client.options.MinecraftInputMapping;
import pw.rebux.parkourdisplay.common.platform.InputController;
import pw.rebux.parkourdisplay.core.util.MinecraftInputUtil;

/// LabyMod-backed [InputController], delegating to the shared [MinecraftInputUtil] key mappings.
public final class LabyInputController implements InputController {

  private final MinecraftInputUtil inputUtil;

  public LabyInputController(MinecraftInputUtil inputUtil) {
    this.inputUtil = inputUtil;
  }

  private MinecraftInputMapping mapping(MovementKey key) {
    return switch (key) {
      case FORWARD -> this.inputUtil.forwardKey();
      case BACK -> this.inputUtil.backKey();
      case LEFT -> this.inputUtil.leftKey();
      case RIGHT -> this.inputUtil.rightKey();
      case SPRINT -> this.inputUtil.sprintKey();
      case SNEAK -> this.inputUtil.sneakKey();
      case JUMP -> this.inputUtil.jumpKey();
    };
  }

  @Override
  public boolean isDown(MovementKey key) {
    return this.mapping(key).isDown();
  }

  @Override
  public void setPressed(MovementKey key, boolean pressed) {
    this.inputUtil.setPressed(this.mapping(key), pressed);
  }
}
