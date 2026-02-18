package pw.rebux.parkourdisplay.core.landingblock;

import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.math.vector.DoubleVector3;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.BoundingBoxUtils;
import pw.rebux.parkourdisplay.core.util.MathHelper;
import pw.rebux.parkourdisplay.core.util.RenderUtils;

@RequiredArgsConstructor
public final class LandingBlockListener {

  private static final double maxCheckDistance = 2;

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var state = this.addon.playerState();

    if (event.phase() != Phase.POST || player == null || state.vy() >= 0) {
      return;
    }

    for (LandingBlock landingBlock : this.addon.landingBlockRegistry().landingBlocks()) {
      var tickPosition = landingBlock.mode() == LandingBlockMode.Land
          ? this.addon.playerState().lastTick()
          : this.addon.playerState().currentTick();

      DoubleVector3 currentBest = null;

      for (var box : landingBlock.blockCollisions()) {
        var isTryingToLandOn = state.currentTick().playerBoundingBox().getMinY() <= box.getMaxY()
            && state.lastTick().playerBoundingBox().getMinY() > box.getMaxY();
        var isInRange = box.getCenter().distanceSquared(player.position().toDoubleVector3()) <= maxCheckDistance;

        if (!isTryingToLandOn || !isInRange) {
          return;
        }

        var offset = BoundingBoxUtils.computeOverlap(tickPosition.playerBoundingBox(), box);

        if (currentBest == null || MathHelper.offsetDistance(offset) > MathHelper.offsetDistance(currentBest)) {
          currentBest = offset;
        }
      }

      if (currentBest != null) {
        this.update(landingBlock, currentBest);
      }
    }
  }

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    if (!this.addon.configuration().highlightLandingBlocks().get()) {
      return;
    }

    for (var landingBlock : this.addon.landingBlockRegistry().landingBlocks()) {
      for (var boundingBox : landingBlock.blockCollisions()) {
        RenderUtils.renderAbsoluteBoundingBox(
            event.camera().renderPosition(),
            boundingBox,
            this.addon.configuration().landingBlockOutlineThickness().get(),
            event.stack(),
            this.addon.configuration().landingBlockFillColor().get().get(),
            this.addon.configuration().landingBlockOutlineColor().get().get()
        );
      }
    }
  }

  private void update(LandingBlock landingBlock, DoubleVector3 offset) {
    var landingBlockRegistry = this.addon.landingBlockRegistry();
    var format = this.addon.decimalFormat();

    var distance = MathHelper.offsetDistance(offset);
    var formattedX = String.format(format, offset.getX());
    var formattedZ = String.format(format, offset.getZ());
    var formattedTotal = String.format(format, distance);

    var newBest = landingBlock.bestDistance() == null || distance > landingBlock.bestDistance();

    if (newBest) {
      this.addon.displayTranslatableWithPrefix("messages.lb.newPB", formattedTotal, formattedX, formattedZ);
      landingBlock.bestDistance(distance);
    } else if (this.addon.configuration().showLandingBlockOffsets().get()) {
      var translatableKey = distance > 0 ? "messages.lb.offsetsHit" : "messages.lb.offsetsMiss";
      this.addon.displayTranslatableWithPrefix(translatableKey, formattedX, formattedZ);
    }

    landingBlockRegistry.lastTotalLandingBlockOffset(distance);
    landingBlockRegistry.lastLandingBlockOffsetX(offset.getX());
    landingBlockRegistry.lastLandingBlockOffsetZ(offset.getZ());
  }
}
