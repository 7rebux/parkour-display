package pw.rebux.parkourdisplay.core.macro;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.entity.player.GameMode;
import net.labymod.api.client.entity.player.Player;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Priority;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.gui.screen.ScreenDisplayEvent;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.GameRenderEvent;
import net.labymod.api.util.math.MathHelper;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.RotationSpline;

@RequiredArgsConstructor
public final class MacroListener {

  private final ParkourDisplayAddon addon;

  private final RotationSpline yawSpline = new RotationSpline();
  private final RotationSpline pitchSpline = new RotationSpline();

  private boolean macroFinished = false;
  private boolean interpolatingRotation = false;
  private boolean screenOpen = false;

  @Subscribe
  public void onScreenDisplay(ScreenDisplayEvent event) {
    this.screenOpen = event.getScreen() != null;
  }

  @Subscribe(Priority.LATE)
  public void onGameTick(GameTickEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var macroRunner = this.addon.macroRunner();
    var activeMacro = macroRunner.activeMacro();
    var inputUtil = this.addon.minecraftInputUtil();

    if (this.macroFinished) {
      inputUtil.unpressAll();
      this.macroFinished = false;
      return;
    }

    if (activeMacro.isEmpty()
        || event.phase() == Phase.POST
        || player == null
        || player.gameMode() == GameMode.SPECTATOR
    ) {
      return;
    }

    // Cancel macro execution if the game is paused (singleplayer) or a screen
    // (pause menu, inventory, chat, ...) is open, which also happens in multiplayer
    if (this.addon.labyAPI().minecraft().isPaused() || this.screenOpen) {
      activeMacro.clear();
      this.macroFinished = true;
      this.stopInterpolating();
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

    float yaw;
    float pitch;

    if (this.hasValidInterpolation()) {
      // Already calculated while interpolating towards this tick
      yaw = this.yawSpline.target();
      pitch = this.pitchSpline.target();
    } else {
      yaw = this.resolveRotation(player.getRotationYaw(), tickInput.yaw());
      pitch = this.resolveRotation(player.getRotationPitch(), tickInput.pitch());
    }

    this.setRotation(player, yaw, pitch);

    // The spline needs the tick after the next one as well to get its outgoing tangent
    var iterator = activeMacro.iterator();
    var nextTickInput = iterator.hasNext() ? iterator.next() : null;
    var followingTickInput = iterator.hasNext() ? iterator.next() : null;

    if (nextTickInput == null || !this.addon.configuration().smoothRotation().get()) {
      this.stopInterpolating();
    } else {
      var nextYaw = this.resolveRotation(yaw, nextTickInput.yaw());
      var nextPitch = this.resolveRotation(pitch, nextTickInput.pitch());

      this.yawSpline.advance(yaw, nextYaw, followingTickInput == null
          ? nextYaw
          : this.resolveRotation(nextYaw, followingTickInput.yaw()));
      this.pitchSpline.advance(pitch, nextPitch, followingTickInput == null
          ? nextPitch
          : this.resolveRotation(nextPitch, followingTickInput.pitch()));

      this.interpolatingRotation = true;
    }

    if (activeMacro.isEmpty()) {
      macroFinished = true;
    }
  }

  @Subscribe
  public void onGameRender(GameRenderEvent event) {
    if (event.phase() != Phase.PRE || !this.hasValidInterpolation()) {
      return;
    }

    var minecraft = this.addon.labyAPI().minecraft();
    var player = minecraft.getClientPlayer();

    if (player == null) {
      this.stopInterpolating();
      return;
    }

    var partialTicks = MathHelper.clamp(minecraft.getPartialTicks(), 0.0F, 1.0F);

    this.setRotation(
        player,
        this.yawSpline.valueAt(partialTicks),
        // The spline can overshoot its control points, which would flip the camera upside down
        MathHelper.clamp(this.pitchSpline.valueAt(partialTicks), -90.0F, 90.0F)
    );
  }

  private boolean hasValidInterpolation() {
    return this.interpolatingRotation;
  }

  private void stopInterpolating() {
    this.interpolatingRotation = false;
    this.yawSpline.reset();
    this.pitchSpline.reset();
  }

  private float resolveRotation(float current, float change) {
    return this.addon.configuration().rotationChange().get() == MacroRotationChange.Relative
        ? current + change
        : change;
  }

  private void setRotation(Player player, float yaw, float pitch) {
    player.setRotationYaw(yaw);
    player.setRotationPitch(pitch);
    // Otherwise the renderer would interpolate towards the value we just set a second time
    player.setPreviousRotationYaw(yaw);
    player.setPreviousRotationPitch(pitch);
  }
}
