package pw.rebux.parkourdisplay.common.engine;

import lombok.RequiredArgsConstructor;
import pw.rebux.parkourdisplay.common.geom.MathUtil;
import pw.rebux.parkourdisplay.common.macro.MacroRotationChange;
import pw.rebux.parkourdisplay.common.macro.MacroRunner;
import pw.rebux.parkourdisplay.common.platform.GameModeType;
import pw.rebux.parkourdisplay.common.platform.InputController.MovementKey;
import pw.rebux.parkourdisplay.common.platform.ParkourContext;
import pw.rebux.parkourdisplay.common.platform.PlayerAccess;
import pw.rebux.parkourdisplay.common.util.RotationSpline;

/// Steps the active macro one tick at a time — pressing/unpressing the movement keys and driving
/// the player rotation, with optional Catmull-Rom smoothing between ticks. The platform calls
/// [#onTick] for every game-tick phase (LabyMod `Priority.LATE`, fired both pre and post) and
/// [#onRenderFrame] once per rendered frame before the world is drawn (LabyMod `GameRenderEvent`
/// `Phase.PRE`). Keys are stepped only on the pre phase (`post == false`); the finished-macro
/// key release runs on whichever phase fires first, matching the original behaviour.
@RequiredArgsConstructor
public final class MacroEngine {

  private final ParkourContext context;
  private final MacroRunner macroRunner;

  private final RotationSpline yawSpline = new RotationSpline();
  private final RotationSpline pitchSpline = new RotationSpline();

  private boolean macroFinished = false;
  private boolean interpolatingRotation = false;

  /// @param post whether this is the post-tick phase; stepping only happens on the pre phase.
  public void onTick(boolean post) {
    var player = this.context.player();
    var activeMacro = this.macroRunner.activeMacro();
    var input = this.context.input();

    if (this.macroFinished) {
      input.unpressAll();
      this.macroFinished = false;
      return;
    }

    if (activeMacro.isEmpty()
        || post
        || player == null
        || player.gameMode() == GameModeType.SPECTATOR
    ) {
      return;
    }

    // Cancel macro execution if the game is paused
    // TODO: This only works in singleplayer
    if (this.context.isGamePaused()) {
      activeMacro.clear();
      this.macroFinished = true;
      this.stopInterpolating();
      return;
    }

    var tickInput = activeMacro.pop();

    input.setPressed(MovementKey.FORWARD, tickInput.w());
    input.setPressed(MovementKey.LEFT, tickInput.a());
    input.setPressed(MovementKey.BACK, tickInput.s());
    input.setPressed(MovementKey.RIGHT, tickInput.d());
    input.setPressed(MovementKey.JUMP, tickInput.jump());
    input.setPressed(MovementKey.SPRINT, tickInput.sprint());
    input.setPressed(MovementKey.SNEAK, tickInput.sneak());

    float yaw;
    float pitch;

    if (this.hasValidInterpolation()) {
      // Already calculated while interpolating towards this tick
      yaw = this.yawSpline.target();
      pitch = this.pitchSpline.target();
    } else {
      yaw = this.resolveRotation(player.yaw(), tickInput.yaw());
      pitch = this.resolveRotation(player.pitch(), tickInput.pitch());
    }

    this.setRotation(player, yaw, pitch);

    // The spline needs the tick after the next one as well to get its outgoing tangent
    var iterator = activeMacro.iterator();
    var nextTickInput = iterator.hasNext() ? iterator.next() : null;
    var followingTickInput = iterator.hasNext() ? iterator.next() : null;

    if (nextTickInput == null || !this.context.config().smoothRotation()) {
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
      this.macroFinished = true;
    }
  }

  public void onRenderFrame(float partialTicks) {
    if (!this.hasValidInterpolation()) {
      return;
    }

    var player = this.context.player();

    if (player == null) {
      this.stopInterpolating();
      return;
    }

    var clampedPartialTicks = MathUtil.clamp(partialTicks, 0.0F, 1.0F);

    this.setRotation(
        player,
        this.yawSpline.valueAt(clampedPartialTicks),
        // The spline can overshoot its control points, which would flip the camera upside down
        MathUtil.clamp(this.pitchSpline.valueAt(clampedPartialTicks), -90.0F, 90.0F)
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
    return this.context.config().rotationChange() == MacroRotationChange.Relative
        ? current + change
        : change;
  }

  private void setRotation(PlayerAccess player, float yaw, float pitch) {
    player.setRotationYaw(yaw);
    player.setRotationPitch(pitch);
    // Otherwise the renderer would interpolate towards the value we just set a second time
    player.setPreviousRotationYaw(yaw);
    player.setPreviousRotationPitch(pitch);
  }
}
