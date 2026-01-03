package pw.rebux.parkourdisplay.core.listener;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.RenderUtil;

@RequiredArgsConstructor
public class RenderWorldListener {

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    renderLandingBlocks(event);
    renderSplitBoxes(event);
  }

  private void renderLandingBlocks(RenderWorldEvent event) {
    if (!addon.configuration().highlightLandingBlocks().get()) {
      return;
    }

    var landingBlocks = this.addon.landingBlockManager().getLandingBlocks();

    for (var landingBlock : landingBlocks) {
      RenderUtil.renderBoundingBox(
          landingBlock.blockPosition(),
          event.camera().renderPosition(),
          Objects.requireNonNull(landingBlock.boundingBox()),
          this.addon.configuration().landingBlockOutlineThickness().get(),
          event.stack(),
          this.addon.configuration().landingBlockFillColor().get().get(),
          this.addon.configuration().landingBlockOutlineColor().get().get());
    }
  }

  // TODO: These are actually planes not boxes, so we can improve the render logic here
  private void renderSplitBoxes(RenderWorldEvent event) {
    if (!addon.configuration().highlightRunSplits().get()) {
      return;
    }

    var splits = this.addon.playerParkourState().runSplits();
    var runEndSplit = this.addon.playerParkourState().runEndSplit();

    for (var split : splits) {
      RenderUtil.renderBoundingBox(
          split.positionOffset().positionVector(),
          event.camera().renderPosition(),
          split.positionOffset().boundingBox(),
          this.addon.configuration().runSplitOutlineThickness().get(),
          event.stack(),
          this.addon.configuration().runSplitFillColor().get().get(),
          this.addon.configuration().runSplitOutlineColor().get().get());
    }

    if (runEndSplit != null) {
      RenderUtil.renderBoundingBox(
          runEndSplit.positionOffset().positionVector(),
          event.camera().renderPosition(),
          runEndSplit.positionOffset().boundingBox(),
          this.addon.configuration().runSplitOutlineThickness().get(),
          event.stack(),
          this.addon.configuration().runSplitFillColor().get().get(),
          this.addon.configuration().runSplitOutlineColor().get().get());
    }
  }
}
