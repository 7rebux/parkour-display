package pw.rebux.parkourdisplay.core.run;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.Color;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.macro.TickInput;
import pw.rebux.parkourdisplay.core.run.split.RunSplit;
import pw.rebux.parkourdisplay.core.util.RenderUtils;
import pw.rebux.parkourdisplay.core.util.TickPosition;

// TODO: BB hat coole funktionen die ich vllt benutzen kann für LB auch
@RequiredArgsConstructor
public final class RunListener {

  private static final AxisAlignedBoundingBox playerAABB =
      new AxisAlignedBoundingBox(-0.3, 0, -0.3, 0.3, 1.8, 0.3);

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

    var playerState = this.addon.playerState();
    var run = this.addon.runState();
    var startPosition = run.startPosition();
    var endSplit = run.endSplit();

    if (startPosition == null || endSplit == null) {
      return;
    }

    // TODO: In old versions (< 1.18) the first tick of a sneak tap would not send a move packet
    // TODO: Minimum movement must be 0.03
    var lastTickAtStart =
        startPosition.distanceSquared(playerState.lastTick().toVector()) == 0;
    var currentTickAtStart =
        startPosition.distanceSquared(playerState.currentTick().toVector()) == 0;

    // Handle reset
    if (currentTickAtStart) {
      run.reset();
    }

    // Handle start
    if (!run.runStarted() && lastTickAtStart && !currentTickAtStart) {
      run.reset();
      run.runStarted(true);
    }

    if (!run.runStarted()) {
      return;
    }

    // Track tick data
    run.processTick(buildTickState(player));

    // Handle splits
    for (var split : run.splits()) {
      if (!split.passed() && split.intersects(player.axisAlignedBoundingBox())) {
        split.updatePB(this.addon, run.timer());
        split.passed(true);
      }
    }

    // Handle finish
    if (endSplit.intersects(player.axisAlignedBoundingBox())) {
      run.processFinish();
    }
  }

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var run = this.addon.runState();

    if (player == null || run.startPosition() == null || run.endSplit() == null) {
      return;
    }

    if (addon.configuration().highlightRunSplits().get()) {
      for (var split : run.splits()) {
        this.renderSplit(event, player, split);
      }
      this.renderSplit(event, player, run.endSplit());
    }

    if (this.addon.configuration().showPrevRunTickStates().get()) {
      if (!this.addon.runState().previousTickStates().isEmpty()) {
        RenderUtils.renderAbsoluteBoundingBox(
            event.camera().renderPosition(),
            playerAABB.move(run.startPosition()),
            this.addon.configuration().runSplitOutlineThickness().get(),
            event.stack(),
            Color.GREEN.withAlpha(60).get(),
            Color.GREEN.withAlpha(80).get());

        for (var tickState : run.previousTickStates()) {
          var color = tickState.position().onGround()
              ? Color.YELLOW
              : Color.RED;

          RenderUtils.renderAbsoluteBoundingBox(
              event.camera().renderPosition(),
              tickState.playerBB(),
              this.addon.configuration().runSplitOutlineThickness().get(),
              event.stack(),
              color.withAlpha(60).get(),
              color.withAlpha(80).get());
        }
      }
    }
  }

  private void renderSplit(RenderWorldEvent event, ClientPlayer player, RunSplit split) {
    var intersecting = split.intersects(player.axisAlignedBoundingBox());
    var color = intersecting ? Color.GREEN : Color.RED;

    RenderUtils.renderAbsoluteBoundingBox(
        event.camera().renderPosition(),
        split.boundingBox(),
        this.addon.configuration().runSplitOutlineThickness().get(),
        event.stack(),
        color.withAlpha(60).get(),
        color.withAlpha(80).get()
    );
  }

  /// Builds immutable player state snapshot for the runs tick history.
  ///
  /// @param player The player.
  /// @return A [RunTickState] object containing the player's input, position, and bounding box.
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
    var boundingBox = player.axisAlignedBoundingBox().move(0, 0, 0);

    return new RunTickState(tickInput, tickPosition, boundingBox);
  }
}
