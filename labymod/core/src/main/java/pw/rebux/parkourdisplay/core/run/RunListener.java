package pw.rebux.parkourdisplay.core.run;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import pw.rebux.parkourdisplay.common.run.RunSplit;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.ParkourDisplayConfiguration.HighlightRunSplitsSettings;
import pw.rebux.parkourdisplay.core.platform.LabyGeom;
import pw.rebux.parkourdisplay.core.util.RenderUtils;

/// Forwards LabyMod game ticks to the shared run engine and renders the run's splits and tick
/// states in the world (LabyMod-specific, so it stays here and converts the neutral geometry back
/// into LabyMod's math types).
@RequiredArgsConstructor
public final class RunListener {

  private static final AxisAlignedBoundingBox playerAABB =
      new AxisAlignedBoundingBox(-0.3, 0, -0.3, 0.3, 1.8, 0.3);

  private final ParkourDisplayAddon addon;

  @Subscribe
  public void onGameTickNew(GameTickEvent event) {
    if (event.phase() != Phase.POST) {
      return;
    }

    this.addon.runEngine().onTick();
  }

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    var splitsSettings = this.addon.configuration().highlightRunSplitsSettings();
    var tickStateSettings = this.addon.configuration().highlightRunTickStates();
    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var run = this.addon.runState();

    if (player == null || run.startPosition() == null || run.endSplit() == null) {
      return;
    }

    if (splitsSettings.highlightRegularSplits().get()) {
      for (var split : run.splits()) {
        this.renderSplit(event, player, split, splitsSettings);
      }
    }

    if (splitsSettings.highlightEndSplit().get()) {
      this.renderSplit(event, player, run.endSplit(), splitsSettings);
    }

    if (tickStateSettings.enabled().get()) {
      if (!run.previousTickStates().isEmpty()) {
        RenderUtils.renderAbsoluteBoundingBox(
            event.camera().renderPosition(),
            playerAABB.move(LabyGeom.toLaby(run.startPosition())),
            tickStateSettings.outlineThickness().get() / 1000F,
            event.stack(),
            tickStateSettings.startFillColor().get().get(),
            tickStateSettings.startOutlineColor().get().get()
        );

        for (var tickState : run.previousTickStates()) {
          var onGround = tickState.position().onGround();
          var fillColor =
              onGround
                  ? tickStateSettings.onGroundFillColor().get()
                  : tickStateSettings.regularFillColor().get();
          var outlineColor =
              onGround
                  ? tickStateSettings.onGroundOutlineColor().get()
                  : tickStateSettings.regularOutlineColor().get();

          RenderUtils.renderAbsoluteBoundingBox(
              event.camera().renderPosition(),
              LabyGeom.toLaby(tickState.position().playerBoundingBox()),
              tickStateSettings.outlineThickness().get() / 1000F,
              event.stack(),
              fillColor.get(),
              outlineColor.get()
          );
        }
      }
    }
  }

  private void renderSplit(
      RenderWorldEvent event,
      ClientPlayer player,
      RunSplit split,
      HighlightRunSplitsSettings settings
  ) {
    var intersecting = split.intersects(LabyGeom.toCommon(player.axisAlignedBoundingBox()));
    var fillColor =
        intersecting
            ? settings.intersectingFillColor().get()
            : settings.regularFillColor().get();
    var outlineColor =
        intersecting
            ? settings.intersectingOutlineColor().get()
            : settings.regularOutlineColor().get();

    RenderUtils.renderAbsoluteBoundingBox(
        event.camera().renderPosition(),
        LabyGeom.toLaby(split.boundingBox()),
        settings.outlineThickness().get() / 1000F,
        event.stack(),
        fillColor.get(),
        outlineColor.get()
    );
  }
}
