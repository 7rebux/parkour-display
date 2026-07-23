package pw.rebux.parkourdisplay.common.engine;

import java.util.LinkedList;
import lombok.RequiredArgsConstructor;
import pw.rebux.parkourdisplay.common.platform.InputController.MovementKey;
import pw.rebux.parkourdisplay.common.platform.ParkourContext;
import pw.rebux.parkourdisplay.common.platform.PlayerAccess;
import pw.rebux.parkourdisplay.common.run.RunState;
import pw.rebux.parkourdisplay.common.run.RunTickState;
import pw.rebux.parkourdisplay.common.run.RunTickState.KeyboardInput;
import pw.rebux.parkourdisplay.common.state.PlayerSnapshot;
import pw.rebux.parkourdisplay.common.state.PlayerState;

/// Tracks a run each tick: start/finish detection via bounding-box intersection, tick-state
/// recording and split/PB updates. The platform calls [#onTick] once per game tick (LabyMod's
/// default-priority `GameTickEvent` `Phase.POST`). Rendering of splits and tick states stays on
/// the platform side.
@RequiredArgsConstructor
public final class RunEngine {

  // Before version 1.18, there is a minimum distance for move packets to update their position.
  private static final float MIN_START_DISTANCE = 0.03f;

  private final ParkourContext context;
  private final RunState run;
  private final PlayerState playerState;

  /// Buffers sub-threshold movement ticks at the start (burst) that happen before the run
  /// officially starts, so they can be prepended to the run's tick states.
  private final LinkedList<RunTickState> preStartTickStates = new LinkedList<>();

  public void onTick() {
    var player = this.context.player();
    if (player == null) {
      return;
    }

    var startPosition = this.run.startPosition();
    var endSplit = this.run.endSplit();

    if (startPosition == null || endSplit == null) {
      return;
    }

    var lastTickAtStart =
        startPosition.distance(this.playerState.lastTick().toVector()) <= MIN_START_DISTANCE;
    var currentTickAtStart =
        startPosition.distance(this.playerState.currentTick().toVector()) <= MIN_START_DISTANCE;

    // Handle reset
    if (currentTickAtStart) {
      this.run.reset();
    }

    // Buffer sub-threshold movement ticks (burst) so they can be prepended on start.
    // The timer must not count them, but macros and rendered tick states must include them.
    if (!this.run.runStarted() && currentTickAtStart) {
      var tickMoveDistance = this.playerState.currentTick().toVector()
          .distance(this.playerState.lastTick().toVector());
      var burstTick = tickMoveDistance > 0 && tickMoveDistance <= MIN_START_DISTANCE;

      if (burstTick) {
        this.preStartTickStates.addLast(this.buildTickState(player));
      } else {
        this.preStartTickStates.clear();
      }
    }

    // Handle start
    if (!this.run.runStarted() && lastTickAtStart && !currentTickAtStart) {
      this.run.reset();
      this.run.processBurstTicks(this.preStartTickStates);
      this.run.runStarted(true);
      this.preStartTickStates.clear();
    }

    if (!this.run.runStarted()) {
      return;
    }

    // Track tick data
    this.run.processTick(this.buildTickState(player));

    // Handle splits
    for (var split : this.run.splits()) {
      if (!split.passed() && split.intersects(player.boundingBox())) {
        split.updatePB(this.context, this.run.timer());
        split.passed(true);
      }
    }

    // Handle finish
    if (endSplit.intersects(player.boundingBox())) {
      this.run.processFinish();
    }
  }

  /// Builds an immutable player state snapshot for the run's tick history.
  private RunTickState buildTickState(PlayerAccess player) {
    var input = this.context.input();
    var keyboardInput = new KeyboardInput(
        input.isDown(MovementKey.FORWARD),
        input.isDown(MovementKey.LEFT),
        input.isDown(MovementKey.BACK),
        input.isDown(MovementKey.RIGHT),
        input.isDown(MovementKey.JUMP),
        input.isDown(MovementKey.SPRINT),
        input.isDown(MovementKey.SNEAK)
    );

    return new RunTickState(keyboardInput, PlayerSnapshot.of(player));
  }
}
