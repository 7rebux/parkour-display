package pw.rebux.parkourdisplay.core.run;

import java.util.LinkedList;
import lombok.RequiredArgsConstructor;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.ParkourDisplayConfiguration.HighlightRunSplitsSettings;
import pw.rebux.parkourdisplay.core.run.RunTickState.KeyboardInput;
import pw.rebux.parkourdisplay.core.util.RenderUtils;
import pw.rebux.parkourdisplay.core.util.TickPosition;

@RequiredArgsConstructor
public final class RunListener {

  private static final AxisAlignedBoundingBox playerAABB =
      new AxisAlignedBoundingBox(-0.3, 0, -0.3, 0.3, 1.8, 0.3);
  // Before version 1.18, there is a minimum distance for move packets to update their position.
  private static final float minStartDistance = 0.03f;

  private final ParkourDisplayAddon addon;

  /// Buffers sub-threshold movement ticks at the start (burst) that happen before the run
  /// officially starts, so they can be prepended to the run's tick states.
  private final LinkedList<RunTickState> preStartTickStates = new LinkedList<>();

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

    var lastTickAtStart =
        startPosition.distance(playerState.lastTick().toVector()) <= minStartDistance;
    var currentTickAtStart =
        startPosition.distance(playerState.currentTick().toVector()) <= minStartDistance;

    // Handle reset
    if (currentTickAtStart) {
      run.reset();
    }

    // Buffer sub-threshold movement ticks (burst) so they can be prepended on start.
    // The timer must not count them, but macros and rendered tick states must include them.
    if (!run.runStarted() && currentTickAtStart) {
      var tickMoveDistance = playerState.currentTick().toVector().distance(playerState.lastTick().toVector());
      var burstTick = tickMoveDistance > 0 && tickMoveDistance <= minStartDistance;

      if (burstTick) {
        this.preStartTickStates.addLast(buildTickState(player));
      } else {
        this.preStartTickStates.clear();
      }
    }

    // Handle start
    if (!run.runStarted() && lastTickAtStart && !currentTickAtStart) {
      run.reset();
      run.processBurstTicks(this.preStartTickStates);
      run.runStarted(true);
      this.preStartTickStates.clear();
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
    var splitsSettings = this.addon.configuration().highlightRunSplitsSettings();
    var tickStateSettings = this.addon.configuration().highlightRunTickStates();
    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var run = this.addon.runState();

    if (player == null || run.startPosition() == null || run.endSplit() == null) {
      return;
    }

    if (splitsSettings.highlightRegularSplits().get()) {
      for (var split : run.splits()) {
        this.renderSplit(event, player, split, splitsSettings);
      }
    }

    if (splitsSettings.highlightEndSplit().get()) {
      this.renderSplit(event, player, run.endSplit(), splitsSettings);
    }

    if (tickStateSettings.enabled().get()) {
      if (!this.addon.runState().previousTickStates().isEmpty()) {
        RenderUtils.renderAbsoluteBoundingBox(
            event.camera().renderPosition(),
            playerAABB.move(run.startPosition()),
            tickStateSettings.outlineThickness().get() / 1000F,
            event.stack(),
            tickStateSettings.startFillColor().get().get(),
            tickStateSettings.startOutlineColor().get().get()
        );

        for (var tickState : run.previousTickStates()) {
          var onGround = tickState.position().onGround();
          var fillColor =
              onGround
                  ? tickStateSettings.onGroundFillColor().get()
                  : tickStateSettings.regularFillColor().get();
          var outlineColor =
              onGround
                  ? tickStateSettings.onGroundOutlineColor().get()
                  : tickStateSettings.regularOutlineColor().get();

          RenderUtils.renderAbsoluteBoundingBox(
              event.camera().renderPosition(),
              tickState.position().playerBoundingBox(),
              tickStateSettings.outlineThickness().get() / 1000F,
              event.stack(),
              fillColor.get(),
              outlineColor.get()
          );
        }
      }
    }
  }

  private void renderSplit(
      RenderWorldEvent event,
      ClientPlayer player,
      RunSplit split,
      HighlightRunSplitsSettings settings
  ) {
    var intersecting = split.intersects(player.axisAlignedBoundingBox());
    var fillColor =
        intersecting
            ? settings.intersectingFillColor().get()
            : settings.regularFillColor().get();
    var outlineColor =
        intersecting
            ? settings.intersectingOutlineColor().get()
            : settings.regularOutlineColor().get();

    RenderUtils.renderAbsoluteBoundingBox(
        event.camera().renderPosition(),
        split.boundingBox(),
        settings.outlineThickness().get() / 1000F,
        event.stack(),
        fillColor.get(),
        outlineColor.get()
    );
  }

  /// Builds immutable player state snapshot for the runs tick history.
  ///
  /// @param player The player.
  /// @return A [RunTickState] object containing the player's input, position, and bounding box.
  private RunTickState buildTickState(ClientPlayer player) {
    var inputUtil = this.addon.minecraftInputUtil();
    var input = new KeyboardInput(
        inputUtil.forwardKey().isDown(),
        inputUtil.leftKey().isDown(),
        inputUtil.backKey().isDown(),
        inputUtil.rightKey().isDown(),
        inputUtil.jumpKey().isDown(),
        inputUtil.sprintKey().isDown(),
        inputUtil.sneakKey().isDown()
    );

    return new RunTickState(input, TickPosition.of(player));
  }
}
