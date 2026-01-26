package pw.rebux.parkourdisplay.core.landingblock;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.state.TickPosition;
import pw.rebux.parkourdisplay.core.util.RenderUtil;

@RequiredArgsConstructor
public final class LandingBlockListener {

  private final ParkourDisplayAddon addon;

  // TODO: Duplicated logic
  //       Extract this shit into a separate class which tracks last ticks, airtime and so on
  //       WITH PRIORITY LATEST POST TICK
  private int airTime = 0;
  private final TickPosition lastTick = new TickPosition();

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();

    if (player == null) {
      return;
    }

    if (event.phase() == Phase.PRE) {
      return;
    }

    var vy = player.position().getY() - this.lastTick.y();

    // If the player landed this tick or is still airborne, we increase the air time
    if (!lastTick.onGround() || !player.isOnGround()) {
      airTime++;
    }

    // Player is falling
    if (vy < 0 && airTime > 1) {
      addon.landingBlockManager().checkOffsets(player, lastTick);
    }

    /* EVERYTHING UNDER HERE WILL UPDATE VALUES FOR THE NEXT CALCULATIONS */

    if (player.isOnGround() && airTime > 0) {
      airTime = 0;
    }

    lastTick.x(player.position().getX());
    lastTick.y(player.position().getY());
    lastTick.z(player.position().getZ());
    lastTick.yaw(player.getRotationYaw());
    lastTick.pitch(player.getRotationPitch());
    lastTick.onGround(player.isOnGround());
  }

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    if (!addon.configuration().highlightLandingBlocks().get()) {
      return;
    }

    var landingBlocks = this.addon.landingBlockManager().landingBlocks();

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
}
