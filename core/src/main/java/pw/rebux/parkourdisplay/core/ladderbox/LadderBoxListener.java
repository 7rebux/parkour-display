package pw.rebux.parkourdisplay.core.ladderbox;

import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.Color;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.BoundingBoxUtils;
import pw.rebux.parkourdisplay.core.util.CollisionUtils;
import pw.rebux.parkourdisplay.core.util.MathHelper;
import pw.rebux.parkourdisplay.core.util.RenderUtils;

/// [Ladders and Vines](https://www.mcpk.wiki/wiki/Ladders_and_Vines)
@RequiredArgsConstructor
public final class LadderBoxListener {

  /// Attempts that never came closer than this are treated as unrelated to the box and
  /// are not reported, so that jumps elsewhere in the world stay quiet.
  private static final double maxMissDistance = 2;

  private final ParkourDisplayAddon addon;

  /// Stand-in for vanilla's `horizontalCollision`, which the climb assist requires.
  private boolean pushingIntoObstacle;

  @Subscribe
  public void onTick(GameTickEvent event) {
    // The flag is only final once the movement of the tick has been applied.
    if (event.phase() != Phase.POST) {
      return;
    }

    var player = this.addon.labyAPI().minecraft().getClientPlayer();

    if (player == null) {
      return;
    }

    this.pushingIntoObstacle = CollisionUtils.isPushingIntoObstacle(player);

    var state = this.addon.playerState();
    var currentTick = state.currentTick();
    // Unlike a landing block, a missed ladder produces no event of its own. The airborne
    // stretch is the attempt instead: every tick of it is sampled, and the closest approach
    // is resolved once the stretch ends, whether the ladder was caught or not.
    var grabbed = currentTick.onClimbable() && !state.lastTick().onClimbable();
    var resolving = grabbed || state.isLandTick();

    for (var ladderBox : this.addon.ladderBoxRegistry().ladderBoxes()) {
      // The grab tick itself carries the best overlap, so sampling covers it before resolving.
      if (!currentTick.onGround()) {
        var offset = BoundingBoxUtils.computeOverlap(
            climbTestBox(currentTick.playerBoundingBox()),
            ladderBox.intersectionBox()
        );

        // Y gates which ticks count rather than contributing to the offset, exactly like the
        // landing tick does for a landing block. A tick spent above or below the column says
        // nothing about horizontal aim, and because the player passes through the column's Y
        // span every flight, counting those ticks would report a near miss off the tick that
        // merely happened to clip the boundary.
        if (offset.getY() >= 0) {
          var previous = ladderBox.attemptOffset();

          if (previous == null
              || MathHelper.offsetDistance(offset) > MathHelper.offsetDistance(previous)) {
            ladderBox.attemptOffset(offset);
          }
        }
      }

      if (resolving) {
        this.resolve(ladderBox);
      } else if (currentTick.onGround()) {
        // Keeps an attempt that never resolved, e.g. because the player was teleported out of
        // it, from bleeding into the next one.
        ladderBox.attemptOffset(null);
      }
    }
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

      // Rendered from the live hitbox rather than the tick snapshot, so the feedback follows
      // the player smoothly between ticks.
      var overlap = BoundingBoxUtils.computeOverlap(
          climbTestBox(player.axisAlignedBoundingBox()),
          ladderBox.intersectionBox()
      );
      // TODO: Do it like this or add epsilon offset to the intersection box?
      var intersectingOrTouching =
          overlap.getX() >= 0 && overlap.getY() >= 0 && overlap.getZ() >= 0;
      // Green once the climb assist would fire, yellow while only the box is intersected.
      var color = intersectingOrTouching
          ? (this.pushingIntoObstacle ? Color.GREEN : Color.YELLOW)
          : Color.RED;

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

  /// The player volume the climb test is measured against: the hitbox flattened onto its
  /// bottom face. Returns a new box, as the caller's may be a stored tick snapshot.
  private static AxisAlignedBoundingBox climbTestBox(AxisAlignedBoundingBox playerBox) {
    return new AxisAlignedBoundingBox(
        playerBox.getMinX(),
        playerBox.getMinY(),
        playerBox.getMinZ(),
        playerBox.getMaxX(),
        playerBox.getMinY(),
        playerBox.getMaxZ()
    );
  }

  private void resolve(LadderBox ladderBox) {
    var offset = ladderBox.attemptOffset();

    if (offset == null) {
      return;
    }

    ladderBox.attemptOffset(null);

    var distance = MathHelper.offsetDistance(offset);

    if (distance < -maxMissDistance) {
      return;
    }

    var ladderBoxRegistry = this.addon.ladderBoxRegistry();

    this.addon.offsetReporter().report(
        "messages.ladderbox",
        ladderBox.best(),
        distance,
        this.addon.configuration().showLadderBoxOffsets().get(),
        offset.getX(),
        offset.getZ()
    );

    ladderBoxRegistry.lastTotalLadderBoxOffset(distance);
    ladderBoxRegistry.lastLadderBoxOffsetX(offset.getX());
    ladderBoxRegistry.lastLadderBoxOffsetZ(offset.getZ());
  }
}
