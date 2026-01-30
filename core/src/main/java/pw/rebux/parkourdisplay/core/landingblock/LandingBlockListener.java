package pw.rebux.parkourdisplay.core.landingblock;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.RenderUtils;

@RequiredArgsConstructor
public final class LandingBlockListener {

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var state = this.addon.playerState();

    if (event.phase() != Phase.POST || player == null) {
      return;
    }

    // Player is falling
    if (state.vy() < 0 && state.airTime() > 1) {
      addon.landingBlockManager().checkOffsets(player, state.lastTick());
    }
  }

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    if (!addon.configuration().highlightLandingBlocks().get()) {
      return;
    }

    var landingBlocks = this.addon.landingBlockManager().landingBlocks();

    for (var landingBlock : landingBlocks) {
      RenderUtils.renderBoundingBox(
          landingBlock.blockPosition(),
          event.camera().renderPosition(),
          Objects.requireNonNull(landingBlock.boundingBox()),
          this.addon.configuration().landingBlockOutlineThickness().get(),
          event.stack(),
          this.addon.configuration().landingBlockFillColor().get().get(),
          this.addon.configuration().landingBlockOutlineColor().get().get());
    }
  }
}
