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

      var box = landingBlock.collisionBox();
      var isTryingToLandOn = state.currentTick().playerBoundingBox().getMinY() <= box.getMaxY()
          && state.lastTick().playerBoundingBox().getMinY() > box.getMaxY();
      var isInRange = box.getCenter().distanceSquared(player.position().toDoubleVector3()) <= maxCheckDistance;

      if (!isTryingToLandOn || !isInRange) {
        continue;
      }

      var offset = BoundingBoxUtils.computeOverlap(tickPosition.playerBoundingBox(), box);
      this.update(landingBlock, offset);
    }
  }

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    var settings = this.addon.configuration().highlightLandingBlocksSettings();

    if (!settings.enabled().get()) {
      return;
    }

    for (var landingBlock : this.addon.landingBlockRegistry().landingBlocks()) {
      RenderUtils.renderAbsoluteBoundingBox(
          event.camera().renderPosition(),
          landingBlock.collisionBox(),
          settings.outlineThickness().get() / 1000F,
          event.stack(),
          settings.fillColor().get().get(),
          settings.outlineColor().get().get()
      );
    }
  }

  private void update(LandingBlock landingBlock, DoubleVector3 offset) {
    var landingBlockRegistry = this.addon.landingBlockRegistry();
    var distance = MathHelper.offsetDistance(offset);

    this.addon.offsetReporter().report(
        "messages.lb",
        landingBlock.best(),
        distance,
        this.addon.configuration().showLandingBlockOffsets().get(),
        offset.getX(),
        offset.getZ()
    );

    landingBlockRegistry.lastTotalLandingBlockOffset(distance);
    landingBlockRegistry.lastLandingBlockOffsetX(offset.getX());
    landingBlockRegistry.lastLandingBlockOffsetZ(offset.getZ());
  }
}
