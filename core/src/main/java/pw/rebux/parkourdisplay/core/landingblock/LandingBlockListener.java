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
import pw.rebux.parkourdisplay.core.state.PlayerState;
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

    if (event.phase() != Phase.POST || player == null) {
      return;
    }

    for (LandingBlock landingBlock : this.addon.landingBlockRegistry().landingBlocks()) {
      var tickPosition = switch (landingBlock.mode()) {
        case Land -> state.lastTick();
        case Hit, Enter -> state.currentTick();
      };
      var box = landingBlock.collisionBox();
      var isTryingToLandOn = isTryingToLandOn(state, landingBlock);
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

  private boolean isTryingToLandOn(PlayerState state, LandingBlock landingBlock) {
    var box = landingBlock.collisionBox();
    var current = state.currentTick().playerBoundingBox();
    var last = state.lastTick().playerBoundingBox();

    return switch (landingBlock.mode()) {
      case Land, Hit -> current.getMinY() <= box.getMaxY()
          && last.getMinY() > box.getMaxY();
      case Enter -> current.getMinY() < box.getMaxY()
          && current.getMinY() >= box.getMinY()
          && current.getMinY() < last.getMinY();
    };
  }

  private void update(LandingBlock landingBlock, DoubleVector3 offset) {
    var landingBlockRegistry = this.addon.landingBlockRegistry();
    var format = this.addon.configuration().decimalFormat();

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
      var color = distance > 0 ? NamedTextColor.DARK_GREEN : NamedTextColor.DARK_RED;
      ChatMessage.of("messages.lb.offsets")
          .withColor(distance > 0 ? NamedTextColor.GREEN : NamedTextColor.RED)
          .withArgs(Component.text(formattedX, color), Component.text(formattedZ, color))
          .send();
    }

    landingBlockRegistry.lastTotalLandingBlockOffset(distance);
    landingBlockRegistry.lastLandingBlockOffsetX(offset.getX());
    landingBlockRegistry.lastLandingBlockOffsetZ(offset.getZ());
  }
}
