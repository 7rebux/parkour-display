package pw.rebux.parkourdisplay.core.ladderbox;

import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.Color;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.BoundingBoxUtils;
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

    // TODO: Intersection offsets
  }

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    var settings = this.addon.configuration().highlightLadderBoxesSettings();

    if (!settings.enabled().get()) {
      return;
    }

    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var ladderBoxes = this.addon.ladderBoxRegistry().ladderBoxes();

    if (player == null) {
      return;
    }

    for (var ladderBox : ladderBoxes) {
      RenderUtils.renderAbsoluteBoundingBox(
          event.camera().renderPosition(),
          ladderBox.boundingBox(),
          settings.outlineThickness().get() / 1000F,
          event.stack(),
          settings.fillColor().get().get(),
          settings.outlineColor().get().get()
      );

      var overlap = BoundingBoxUtils.computeOverlap(
          player.axisAlignedBoundingBox().maxY(player.axisAlignedBoundingBox().getMinY()),
          ladderBox.intersectionBox());
      // TODO: Do it like this or add epsilon offset to the intersection box?
      var intersectingOrTouching =
          overlap.getX() >= 0 && overlap.getY() >= 0 && overlap.getZ() >= 0;
      var color = intersectingOrTouching ? Color.GREEN : Color.RED;

      RenderUtils.renderAbsoluteBoundingBox(
          event.camera().renderPosition(),
          ladderBox.intersectionBox(),
          settings.outlineThickness().get() / 1000F,
          event.stack(),
          color.withAlpha(30).get(),
          color.get()
      );
    }
  }
}
