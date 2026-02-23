package pw.rebux.parkourdisplay.core.ladderbox;

import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.Color;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.RenderUtils;

/// [Ladders and Vines](https://www.mcpk.wiki/wiki/Ladders_and_Vines)
@RequiredArgsConstructor
public final class LadderBoxListener {

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onTick(GameTickEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();

    if (player == null) {
      return;
    }
  }

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var ladderBoxes = this.addon.ladderBoxRegistry().ladderBoxes();

    if (player == null) {
      return;
    }

    for (var ladderBox : ladderBoxes) {
      RenderUtils.renderAbsoluteBoundingBox(
          event.camera().renderPosition(),
          ladderBox.boundingBox(),
          this.addon.configuration().landingBlockOutlineThickness().get(),
          event.stack(),
          this.addon.configuration().landingBlockFillColor().get().get(),
          this.addon.configuration().landingBlockOutlineColor().get().get()
      );

      // TODO: Should be touching or intersecting
      var intersecting = ladderBox.intersectionBox().intersects(
          player.axisAlignedBoundingBox().maxY(player.axisAlignedBoundingBox().getMinY()));
      var color = intersecting ? Color.GREEN : Color.RED;

      RenderUtils.renderAbsoluteBoundingBox(
          event.camera().renderPosition(),
          ladderBox.intersectionBox(),
          this.addon.configuration().landingBlockOutlineThickness().get(),
          event.stack(),
          color.withAlpha(30).get(),
          color.get()
      );
    }
  }
}
