package pw.rebux.parkourdisplay.core.ladder;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.labymod.api.Laby;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.IntVector3;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.TickPosition;
import pw.rebux.parkourdisplay.core.util.WorldUtils;

/// Records every airborne period and fires a LadderClimbEvent when one that touched a ladder
/// lands.
@RequiredArgsConstructor
public final class LadderTracker {

  // Flying would otherwise grow the recording forever.
  private static final int MAX_TICKS = 5 * 60 * 20;

  private final ParkourDisplayAddon addon;
  private final List<TickPosition> ticks = new ArrayList<>();

  private boolean climbed = false;
  private double ladderBottomY = 0;
  private double ladderTopY = 0;

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    if (event.phase() != Phase.POST) {
      return;
    }

    if (this.addon.labyAPI().minecraft().getClientPlayer() == null) {
      return;
    }

    var state = this.addon.playerState();

    if (state.airTime() == 0) {
      this.reset();
      return;
    }

    if (state.airTime() == 1) {
      this.reset();
      this.ticks.add(state.lastTick());
    }

    if (this.ticks.size() > MAX_TICKS) {
      return;
    }

    this.ticks.add(state.currentTick());

    // Walking into a ladder is already on it when leaving the ground.
    if (state.currentTick().onClimbable()
        && (!this.climbed || !state.lastTick().onClimbable())) {
      this.climbed = true;
      this.updateLadderBounds(state.currentTick());
    }

    if (state.currentTick().onGround() && this.climbed) {
      Laby.fireEvent(new LadderClimbEvent(
          new LadderClimb(List.copyOf(this.ticks), this.ladderBottomY, this.ladderTopY)));
    }
  }

  private void reset() {
    this.ticks.clear();
    this.climbed = false;
  }

  private void updateLadderBounds(TickPosition climbTick) {
    var feet = new IntVector3(
        (int) Math.floor(climbTick.x()),
        (int) Math.floor(climbTick.y()),
        (int) Math.floor(climbTick.z())
    );
    var ladders = WorldUtils.findConnectedLadders(feet);
    this.ladderTopY = ladders.stream()
        .mapToDouble(AxisAlignedBoundingBox::getMaxY)
        .max()
        .orElse(Math.floor(climbTick.y()) + 1);
    this.ladderBottomY = ladders.stream()
        .mapToDouble(AxisAlignedBoundingBox::getMinY)
        .min()
        .orElse(Math.floor(climbTick.y()));
  }
}
