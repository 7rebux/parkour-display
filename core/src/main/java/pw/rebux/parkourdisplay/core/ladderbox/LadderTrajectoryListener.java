package pw.rebux.parkourdisplay.core.ladderbox;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.gfx.pipeline.renderer.text.TextRenderingOptions;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.overlay.IngameOverlayRenderEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.Color;
import net.labymod.api.util.bounds.Rectangle;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.RenderUtils;

/// Records the per-tick positions of airborne stretches that touch a climbable and renders
/// them as dots in the world. Looking at a dot shows the state of that tick next to the
/// crosshair.
@RequiredArgsConstructor
public final class LadderTrajectoryListener {

  /// Keeps a long climb or a flight that never lands from growing without bound.
  private static final int maxTicks = 400;

  private static final double dotRadius = 0.03;

  /// How far the crosshair ray may pass a dot's center and still hover it. Slightly larger than
  /// the dot itself so that small dots stay easy to aim at.
  private static final double hoverRadius = 0.05;

  /// Vertical speed the ladder climb assist forces: (0.2 - gravity 0.08) * drag 0.98.
  private static final double ladderAssistSpeed = 0.1176;
  private static final double assistEpsilon = 0.005;

  private static final int tooltipPadding = 3;

  private final ParkourDisplayAddon addon;

  /// Ticks of the airborne stretch currently in progress.
  private final List<TrajectoryTick> currentTrajectory = new ArrayList<>();

  /// The last completed airborne stretch that touched a climbable.
  private final List<TrajectoryTick> lastTrajectory = new ArrayList<>();

  private boolean currentTouchedClimbable;

  /// The dot under the crosshair, resolved every frame during world rendering.
  @Nullable
  private TrajectoryTick hovered;

  @Subscribe
  public void onTick(GameTickEvent event) {
    if (event.phase() != Phase.POST) {
      return;
    }

    var player = this.addon.labyAPI().minecraft().getClientPlayer();

    if (player == null) {
      return;
    }

    var state = this.addon.playerState();
    var currentTick = state.currentTick();

    // Fully grounded ticks are not part of any trajectory.
    if (currentTick.onGround() && state.lastTick().onGround()) {
      return;
    }

    if (state.isJumpTick()) {
      this.currentTrajectory.clear();
      this.currentTouchedClimbable = false;
    }

    var inputUtil = this.addon.minecraftInputUtil();

    this.currentTrajectory.add(new TrajectoryTick(
        state.airTime(),
        currentTick.x(), currentTick.y(), currentTick.z(),
        state.vx(), state.vy(), state.vz(),
        currentTick.yaw(),
        inputUtil.forwardKey().isDown(),
        inputUtil.leftKey().isDown(),
        inputUtil.backKey().isDown(),
        inputUtil.rightKey().isDown(),
        inputUtil.jumpKey().isDown(),
        inputUtil.sneakKey().isDown(),
        player.isSprinting(),
        currentTick.onGround(),
        currentTick.onClimbable(),
        currentTick.collided()
    ));

    if (this.currentTrajectory.size() > maxTicks) {
      this.currentTrajectory.removeFirst();
    }

    this.currentTouchedClimbable |= currentTick.onClimbable();

    if (state.isLandTick()) {
      if (this.currentTouchedClimbable) {
        this.lastTrajectory.clear();
        this.lastTrajectory.addAll(this.currentTrajectory);
      }

      this.currentTrajectory.clear();
      this.currentTouchedClimbable = false;
    }
  }

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    this.hovered = null;

    if (!this.addon.configuration().showLadderTrajectory().get()) {
      return;
    }

    var trajectory = this.displayedTrajectory();

    if (trajectory.isEmpty()) {
      return;
    }

    var player = this.addon.labyAPI().minecraft().getClientPlayer();

    if (player == null) {
      return;
    }

    var eye = event.camera().renderPosition();
    // The player's rotation rather than the camera's: the camera derives yaw and pitch from its
    // quaternion in a different convention (pitch is positive looking up), which mirrors the
    // ray. In back third person the crosshair ray is parallel to the player's look anyway.
    var yaw = Math.toRadians(player.getRotationYaw());
    var pitch = Math.toRadians(player.getRotationPitch());
    var dirX = -Math.sin(yaw) * Math.cos(pitch);
    var dirY = -Math.sin(pitch);
    var dirZ = Math.cos(yaw) * Math.cos(pitch);

    // The dot whose center passes closest to the crosshair ray wins, so that neighbouring dots
    // that both lie within the hover radius don't flicker between each other.
    var closestDistance = hoverRadius;

    for (var tick : trajectory) {
      var toX = tick.x() - eye.getX();
      var toY = tick.y() - eye.getY();
      var toZ = tick.z() - eye.getZ();
      var along = toX * dirX + toY * dirY + toZ * dirZ;

      if (along <= 0) {
        continue;
      }

      var offX = toX - along * dirX;
      var offY = toY - along * dirY;
      var offZ = toZ - along * dirZ;
      var distance = Math.sqrt(offX * offX + offY * offY + offZ * offZ);

      if (distance < closestDistance) {
        closestDistance = distance;
        this.hovered = tick;
      }
    }

    for (var tick : trajectory) {
      var color = tick == this.hovered ? Color.WHITE : dotColor(tick);
      var radius = tick == this.hovered ? dotRadius * 1.5 : dotRadius;

      RenderUtils.renderAbsoluteBoundingBox(
          eye,
          new AxisAlignedBoundingBox(
              tick.x() - radius, tick.y() - radius, tick.z() - radius,
              tick.x() + radius, tick.y() + radius, tick.z() + radius
          ),
          0.005F,
          event.stack(),
          color.withAlpha(120).get(),
          color.get()
      );
    }
  }

  @Subscribe
  public void onRenderOverlay(IngameOverlayRenderEvent event) {
    var tick = this.hovered;

    if (event.phase() != Phase.POST || event.isGuiHidden() || tick == null) {
      return;
    }

    var format = this.addon.configuration().decimalFormat();
    var vyColor = tick.vy() > 0
        ? NamedTextColor.GREEN
        : tick.vy() < 0 ? NamedTextColor.RED : NamedTextColor.GRAY;

    var flags = new StringBuilder();
    if (tick.onClimbable()) flags.append("[onLadder] ");
    if (tick.onGround()) flags.append("[onGround] ");
    if (tick.collided()) flags.append("[collided] ");

    var lines = new ArrayList<RenderableComponent>();
    lines.add(line("Tick", String.valueOf(tick.tick()), NamedTextColor.WHITE));
    lines.add(line("Y", format.formatted(tick.y()), NamedTextColor.AQUA));
    lines.add(line("vY", format.formatted(tick.vy()), vyColor));
    lines.add(line("X / Z",
        format.formatted(tick.x()) + " / " + format.formatted(tick.z()), NamedTextColor.AQUA));
    lines.add(line("vX / vZ",
        format.formatted(tick.vx()) + " / " + format.formatted(tick.vz()), NamedTextColor.GRAY));
    lines.add(line("Yaw", format.formatted(tick.yaw()), NamedTextColor.GRAY));
    lines.add(line("Input", tick.inputs(), NamedTextColor.YELLOW));

    if (!flags.isEmpty()) {
      lines.add(RenderableComponent.of(
          Component.text(flags.toString().trim(), NamedTextColor.BLUE)));
    }

    var width = 0F;
    var height = 0F;

    for (var line : lines) {
      width = Math.max(width, line.getWidth());
      height += line.getHeight();
    }

    var window = this.addon.labyAPI().minecraft().minecraftWindow();
    // Offset from the crosshair so the tooltip never covers the dot being inspected.
    var x = window.getScaledWidth() / 2F + 8;
    var y = window.getScaledHeight() / 2F + 8;
    var canvas = event.context().canvas();

    canvas.submitRect(
        Rectangle.relative(
            x, y, width + tooltipPadding * 2, height + tooltipPadding * 2),
        0xA0000000
    );

    var lineY = y + tooltipPadding;

    for (var line : lines) {
      canvas.submitRenderableComponent(
          line, x + tooltipPadding, lineY, -1, TextRenderingOptions.SHADOW);
      lineY += line.getHeight();
    }
  }

  /// The in-progress stretch once it touched a climbable, otherwise the last one that did.
  private List<TrajectoryTick> displayedTrajectory() {
    return this.currentTouchedClimbable ? this.currentTrajectory : this.lastTrajectory;
  }

  private static RenderableComponent line(String label, String value, TextColor valueColor) {
    return RenderableComponent.of(
        Component.text(label + ": ", NamedTextColor.GRAY)
            .append(Component.text(value, valueColor))
    );
  }

  /// Colored like the jump graph: cyan while the climb assist drives the tick, yellow on any
  /// other climbable tick, otherwise green when rising and red when falling.
  private static Color dotColor(TrajectoryTick tick) {
    if (tick.onClimbable()) {
      return Math.abs(tick.vy() - ladderAssistSpeed) < assistEpsilon
          ? Color.CYAN
          : Color.YELLOW;
    }

    if (tick.vy() > 0) {
      return Color.GREEN;
    }

    return tick.vy() < 0 ? Color.RED : Color.GRAY;
  }
}
