package pw.rebux.parkourdisplay.core.ladderbox;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.tickdot.TickDot;

/// Records the per-tick states of airborne stretches that touch a climbable. They are rendered
/// as dots by the [pw.rebux.parkourdisplay.core.tickdot.TickDotListener].
@RequiredArgsConstructor
public final class LadderTrajectoryListener {

  /// Keeps a long climb or a flight that never lands from growing without bound.
  private static final int maxTicks = 400;

  private final ParkourDisplayAddon addon;

  /// Ticks of the airborne stretch currently in progress.
  private final List<TickDot> currentTrajectory = new ArrayList<>();

  /// The last completed airborne stretch that touched a climbable.
  private final List<TickDot> lastTrajectory = new ArrayList<>();

  private boolean currentTouchedClimbable;

  @Subscribe
  public void onTick(GameTickEvent event) {
    if (event.phase() != Phase.POST) {
      return;
    }

    var player = this.addon.labyAPI().minecraft().getClientPlayer();

    if (player == null) {
      return;
    }

    var state = this.addon.playerState();
    var currentTick = state.currentTick();

    // Fully grounded ticks are not part of any trajectory.
    if (currentTick.onGround() && state.lastTick().onGround()) {
      return;
    }

    if (state.isJumpTick()) {
      this.currentTrajectory.clear();
      this.currentTouchedClimbable = false;
    }

    this.currentTrajectory.add(TickDot.capture(this.addon, player));

    if (this.currentTrajectory.size() > maxTicks) {
      this.currentTrajectory.removeFirst();
    }

    this.currentTouchedClimbable |= currentTick.onClimbable();

    if (state.isLandTick()) {
      if (this.currentTouchedClimbable) {
        this.lastTrajectory.clear();
        this.lastTrajectory.addAll(this.currentTrajectory);
      }

      this.currentTrajectory.clear();
      this.currentTouchedClimbable = false;
    }
  }

  /// The in-progress stretch once it touched a climbable, otherwise the last one that did.
  public List<TickDot> displayedTrajectory() {
    return this.currentTouchedClimbable ? this.currentTrajectory : this.lastTrajectory;
  }
}
