package pw.rebux.parkourdisplay.core.macro;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.entity.player.GameMode;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Priority;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@RequiredArgsConstructor
public class MacroRunner {

  private final ParkourDisplayAddon addon;

  private boolean macroFinished = false;

  // TODO: Create one listener per feature instead (FeatureAListener)
  @Subscribe(Priority.LATEST)
  public void onGameTick(GameTickEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var activeMacro = this.addon.macroManager().activeMacro();
    var inputUtil = this.addon.minecraftInputUtil();

    if (macroFinished && this.addon.configuration().unpressKeys().get()) {
      inputUtil.unpressAll();
      macroFinished = false;
      return;
    }

    if (activeMacro.isEmpty()
        || event.phase() == Phase.POST
        || player == null
        || player.gameMode() == GameMode.SPECTATOR
    ) {
      return;
    }

    // Cancel macro execution if the game is paused
    if (this.addon.labyAPI().minecraft().isPaused()) {
      activeMacro.clear();
      macroFinished = true;
      return;
    }

    var tickInput = activeMacro.pop();

    inputUtil.setPressed(inputUtil.forwardKey(), tickInput.w());
    inputUtil.setPressed(inputUtil.leftKey(), tickInput.a());
    inputUtil.setPressed(inputUtil.backKey(), tickInput.s());
    inputUtil.setPressed(inputUtil.rightKey(), tickInput.d());
    inputUtil.setPressed(inputUtil.jumpKey(), tickInput.jump());
    inputUtil.setPressed(inputUtil.sprintKey(), tickInput.sprint());
    inputUtil.setPressed(inputUtil.sneakKey(), tickInput.sneak());

    if (this.addon.configuration().rotationChange().get() == MacroRotationChange.RELATIVE) {
      player.setRotationYaw(player.getRotationYaw() + tickInput.yaw());
      player.setRotationPitch(player.getRotationPitch() + tickInput.pitch());
    } else {
      player.setRotationYaw(tickInput.yaw());
      player.setRotationPitch(tickInput.pitch());
    }

    if (activeMacro.isEmpty()) {
      macroFinished = true;
    }
  }
}
