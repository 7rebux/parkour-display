package pw.rebux.parkourdisplay.core.landingblock;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.math.vector.DoubleVector3;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.BoundingBoxUtils;
import pw.rebux.parkourdisplay.core.util.ChatMessage;
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

    // TODO: Also doesn't really make sense for some blocks to compare multiple bounding boxes
    //       since their height and position can differ. Depending on which one the player tries
    //       to land on, it might be better to only specify one bounding box in the landing block.
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
          continue;
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
    var settings = this.addon.configuration().highlightLandingBlocksSettings();

    if (!settings.enabled().get()) {
      return;
    }

    for (var landingBlock : this.addon.landingBlockRegistry().landingBlocks()) {
      for (var boundingBox : landingBlock.blockCollisions()) {
        RenderUtils.renderAbsoluteBoundingBox(
            event.camera().renderPosition(),
            boundingBox,
            settings.outlineThickness().get() / 1000F,
            event.stack(),
            settings.fillColor().get().get(),
            settings.outlineColor().get().get()
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
      landingBlock.bestDistance(distance);
      ChatMessage.of("messages.lb.newPB")
          .withColor(NamedTextColor.GREEN)
          .withArgs(
              Component.text(formattedTotal, NamedTextColor.DARK_GREEN),
              formattedX,
              formattedZ
          )
          .send();
    } else if (this.addon.configuration().showLandingBlockOffsets().get()) {
      ChatMessage.of("messages.lb.offsets")
          .withColor(distance > 0 ? NamedTextColor.GREEN : NamedTextColor.RED)
          .withArgs(
              Component.text(
                  formattedX,
                  distance > 0 ? NamedTextColor.DARK_GREEN : NamedTextColor.DARK_RED
              ),
              Component.text(
                  formattedZ,
                  distance > 0 ? NamedTextColor.DARK_GREEN : NamedTextColor.DARK_RED
              )
          )
          .send();
    }

    landingBlockRegistry.lastTotalLandingBlockOffset(distance);
    landingBlockRegistry.lastLandingBlockOffsetX(offset.getX());
    landingBlockRegistry.lastLandingBlockOffsetZ(offset.getZ());
  }
}
