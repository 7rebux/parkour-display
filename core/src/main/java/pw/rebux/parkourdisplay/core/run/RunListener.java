package pw.rebux.parkourdisplay.core.run;

import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.macro.MacroRotationChange;
import pw.rebux.parkourdisplay.core.macro.TickInput;
import pw.rebux.parkourdisplay.core.util.RenderUtil;

@RequiredArgsConstructor
public final class RunListener {

  // Persisting a maximum of 6.000 ticks (5 minutes)
  private static final int MAX_RUN_TICK_INPUTS = 5 * 60 * 20;

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var state = this.addon.playerState();
    var inputUtil = this.addon.minecraftInputUtil();
    var runState = this.addon.runState();

    if (event.phase() != Phase.POST
        || player == null
        || !runState.isRunSetUp()
    ) {
      return;
    }

    var startOffsetX = Math.abs(runState.runStartPosition().posX() - player.position().getX());
    var startOffsetZ = Math.abs(runState.runStartPosition().posZ() - player.position().getZ());
    var endOffsetX = Math.abs(runState.runEndSplit().positionOffset().posX() - player.position().getX());
    var endOffsetZ = Math.abs(runState.runEndSplit().positionOffset().posZ() - player.position().getZ());

    // Start
    if (startOffsetX <= runState.runStartPosition().offsetX()
        && startOffsetZ <= runState.runStartPosition().offsetZ()
        && runState.runStartPosition().posY() == player.position().getY()
    ) {
      runState.reset();
      runState.runStarted(true);
    }

    // Splits
    if (runState.runStarted()) {
      for (var split : runState.runSplits()) {
        var splitOffsetX = Math.abs(split.positionOffset().posX() - player.position().getX());
        var splitOffsetZ = Math.abs(split.positionOffset().posZ() - player.position().getZ());

        if (!split.passed()
            && splitOffsetX <= split.positionOffset().offsetX() / 2
            && splitOffsetZ <= split.positionOffset().offsetZ() / 2
            && split.positionOffset().posY() == player.position().getY()
        ) {
          split.updatePB(this.addon, runState.runTimer());
          split.passed(true);
        }
      }
    }

    // End
    if (runState.runStarted()
        && endOffsetX <= runState.runEndSplit().positionOffset().offsetX() / 2
        && endOffsetZ <= runState.runEndSplit().positionOffset().offsetZ() / 2
        && runState.runEndSplit().positionOffset().posY() == player.position().getY()
    ) {
      runState.runEndSplit().updatePB(addon, runState.runTimer());
      runState.runStarted(false);
    }

    // Timers etc.
    if (runState.runStarted()) {
      runState.runTimer(runState.runTimer() + 1);

      if (state.lastTick().onGround() && player.isOnGround()) {
        runState.runGroundTime(runState.runGroundTime() + 1);
      }

      if (runState.runTickInputs().size() < MAX_RUN_TICK_INPUTS) {
        var isRelativeRotation =
            this.addon.configuration().rotationChange().get() == MacroRotationChange.Relative;
        var yawChange = player.getRotationYaw() - state.lastTick().yaw();
        var pitchChange = player.getRotationPitch() - state.lastTick().pitch();

        runState.runTickInputs().add(
            new TickInput(
                inputUtil.forwardKey().isDown(),
                inputUtil.leftKey().isDown(),
                inputUtil.backKey().isDown(),
                inputUtil.rightKey().isDown(),
                inputUtil.jumpKey().isDown(),
                inputUtil.sprintKey().isDown(),
                inputUtil.sneakKey().isDown(),
                isRelativeRotation ? yawChange : player.getRotationYaw(),
                isRelativeRotation ? pitchChange: player.getRotationPitch()));
      }
    }
  }

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    if (!addon.configuration().highlightRunSplits().get()) {
      return;
    }

    var splits = this.addon.runState().runSplits();
    for (var split : splits) {
      this.renderSplit(event, split.positionOffset());
    }

    var runStartPosition = this.addon.runState().runStartPosition();
    if (runStartPosition != null) {
      this.renderSplit(event, runStartPosition);
    }

    var runEndSplit = this.addon.runState().runEndSplit();
    if (runEndSplit != null) {
      this.renderSplit(event, runEndSplit.positionOffset());
    }
  }

  private void renderSplit(RenderWorldEvent event, PositionOffset positionOffset) {
    RenderUtil.renderBoundingBox(
        positionOffset.positionVector(),
        event.camera().renderPosition(),
        positionOffset.boundingBox(),
        this.addon.configuration().runSplitOutlineThickness().get(),
        event.stack(),
        this.addon.configuration().runSplitFillColor().get().get(),
        this.addon.configuration().runSplitOutlineColor().get().get());
  }
}
