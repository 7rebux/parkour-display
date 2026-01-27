package pw.rebux.parkourdisplay.core.util;

import lombok.Getter;
import net.labymod.api.client.options.MinecraftInputMapping;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@Getter
public final class MinecraftInputUtil {

  private final MinecraftInputMapping forwardKey;
  private final MinecraftInputMapping backKey;
  private final MinecraftInputMapping leftKey;
  private final MinecraftInputMapping rightKey;
  private final MinecraftInputMapping sprintKey;
  private final MinecraftInputMapping sneakKey;
  private final MinecraftInputMapping jumpKey;

  public MinecraftInputUtil(ParkourDisplayAddon addon) {
    var options = addon.labyAPI().minecraft().options();

    this.forwardKey = options.getInputMapping("key.forward");
    this.backKey = options.getInputMapping("key.back");
    this.leftKey = options.getInputMapping("key.left");
    this.rightKey = options.getInputMapping("key.right");
    this.sprintKey = options.getInputMapping("key.sprint");
    this.sneakKey = options.getInputMapping("key.sneak");
    this.jumpKey = options.getInputMapping("key.jump");
  }

  public void setPressed(MinecraftInputMapping input, boolean pressed) {
    if (pressed) {
      input.press();
    } else {
      input.unpress();
    }
  }

  public boolean isMoving() {
    return forwardKey.isDown() || backKey.isDown() || leftKey.isDown() || rightKey.isDown();
  }

  public void unpressAll() {
    setPressed(forwardKey, false);
    setPressed(backKey, false);
    setPressed(leftKey, false);
    setPressed(rightKey, false);
    setPressed(sprintKey, false);
    setPressed(sneakKey, false);
    setPressed(jumpKey, false);
  }
}
