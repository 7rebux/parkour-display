package pw.rebux.parkourdisplay.core.listener;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.labymod.api.Laby;
import net.labymod.api.client.gfx.GFXBridge;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.RenderUtil;

@RequiredArgsConstructor
public class RenderWorldListener {

  private static final GFXBridge gfx = Laby.references().gfxRenderPipeline().gfx();

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    if (!addon.configuration().highlightLandingBlocks().get()) {
      return;
    }

    var landingBlocks = this.addon.landingBlockManager().getLandingBlocks();

    if (landingBlocks.isEmpty()) {
      return;
    }

    gfx.disableDepth();
    gfx.disableCull();

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

    gfx.enableDepth();
    gfx.enableCull();
  }
}
