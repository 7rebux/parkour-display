package pw.rebux.parkourdisplay.core.run;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.Color;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.macro.TickInput;
import pw.rebux.parkourdisplay.core.run.split.RunSplit;
import pw.rebux.parkourdisplay.core.util.RenderUtils;
import pw.rebux.parkourdisplay.core.util.TickPosition;

// TODO: BB hat coole funktionen die ich vllt benutzen kann für LB auch
@RequiredArgsConstructor
public final class RunListener {

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onGameTickNew(GameTickEvent event) {
    if (event.phase() != Phase.POST) {
      return;
    }

    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    if (player == null) {
      return;
    }

    var run = this.addon.runState();
    var runStartPosition = run.runStartPosition();
    var runEndSplit = run.runEndSplit();

    if (runStartPosition == null) {
      return;
    }
    if (runEndSplit == null) {
      return;
    }

    // Handle start
    var startOffsetX = Math.abs(runStartPosition.posX() - player.position().getX());
    var startOffsetZ = Math.abs(runStartPosition.posZ() - player.position().getZ());

    // TODO: Still a shit check, but still kinda needed until I have a better idea
    if (startOffsetX <= runStartPosition.offsetX()
        && startOffsetZ <= runStartPosition.offsetZ()
        && runStartPosition.posY() == player.position().getY()
    ) {
      run.reset();
      run.runStarted(true);
    }

    if (!run.runStarted()) {
      return;
    }

    // Handle splits
    for (var split : run.runSplits()) {
      if (!split.passed() && split.intersects(player.axisAlignedBoundingBox())) {
        split.updatePB(this.addon, run.timer());
        split.passed(true);
      }
    }

    // Handle finish
    if (runEndSplit.intersects(player.axisAlignedBoundingBox())) {
      run.processFinish();
      return;
    }

    // Track tick data
    run.processTick(buildTickState(player));
  }

  private RunTickState buildTickState(ClientPlayer player) {
    var inputUtil = this.addon.minecraftInputUtil();
    var tickInput = new TickInput(
        inputUtil.forwardKey().isDown(),
        inputUtil.leftKey().isDown(),
        inputUtil.backKey().isDown(),
        inputUtil.rightKey().isDown(),
        inputUtil.jumpKey().isDown(),
        inputUtil.sprintKey().isDown(),
        inputUtil.sneakKey().isDown()
    );
    var tickPosition = new TickPosition(
        player.position().getX(),
        player.position().getY(),
        player.position().getZ(),
        player.getRotationYaw(),
        player.getRotationPitch(),
        player.isOnGround()
    );

    return new RunTickState(tickInput, tickPosition, player.axisAlignedBoundingBox());
  }

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();

    if (player == null) {
      return;
    }

//    for (var tickState : this.addon.runState().previousTickStates()) {
//      var tickPosition = tickState.position();
//      RenderUtils.renderAbsoluteBoundingBox(
//          event.camera().renderPosition(),
//          tickState.playerBB(),
//          this.addon.configuration().runSplitOutlineThickness().get(),
//          event.stack(),
//          this.addon.configuration().runSplitFillColor().get().get(),
//          this.addon.configuration().runSplitOutlineColor().get().get());
//    }

    if (!addon.configuration().highlightRunSplits().get()) {
      return;
    }

    var splits = this.addon.runState().runSplits();
    for (var split : splits) {
      var intersecting = split.intersects(player.axisAlignedBoundingBox());
      this.renderSplit(event, split, intersecting ? Color.GREEN.withAlpha(80).get() : Color.RED.withAlpha(80).get());
    }

    var runStartPosition = this.addon.runState().runStartPosition();
    if (runStartPosition != null) {
      this.renderSplit(event, runStartPosition, this.addon.configuration().runSplitFillColor().get().get());
    }

    var runEndSplit = this.addon.runState().runEndSplit();
    if (runEndSplit != null) {
      var intersecting = runEndSplit.intersects(player.axisAlignedBoundingBox());
      this.renderSplit(event, runEndSplit, intersecting ? Color.GREEN.withAlpha(80).get() : Color.RED.withAlpha(80).get());
    }
  }

  private void renderSplit(RenderWorldEvent event, PositionOffset positionOffset, int color) {
    RenderUtils.renderBoundingBox(
        positionOffset.positionVector(),
        event.camera().renderPosition(),
        positionOffset.boundingBox(),
        this.addon.configuration().runSplitOutlineThickness().get(),
        event.stack(),
        color,
        this.addon.configuration().runSplitOutlineColor().get().get());
  }

  private void renderSplit(RenderWorldEvent event, RunSplit runSplit, int color) {
    RenderUtils.renderAbsoluteBoundingBox(
        event.camera().renderPosition(),
        runSplit.boundingBox(),
        this.addon.configuration().runSplitOutlineThickness().get(),
        event.stack(),
        color,
        this.addon.configuration().runSplitOutlineColor().get().get());
  }
}
