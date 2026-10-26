package pw.rebux.parkourdisplay.core.run;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.gfx.pipeline.renderer.text.TextRenderingOptions;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.entity.player.ClientPlayerInteractEvent;
import net.labymod.api.event.client.entity.player.ClientPlayerInteractEvent.InteractionType;
import net.labymod.api.event.client.render.overlay.IngameOverlayRenderEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.Color;
import net.labymod.api.util.bounds.Rectangle;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.RenderUtils;

/// Renders the start position and the ticks of the previous run as dots in the world, colored by
/// their vertical movement and ladder state. Looking at a dot shows the state of that tick next to
/// the crosshair, hitting it toggles between the dot and the tick's hitbox.
@RequiredArgsConstructor
public final class RunTickDotListener {

  private static final AxisAlignedBoundingBox playerAABB =
      new AxisAlignedBoundingBox(-0.3, 0, -0.3, 0.3, 1.8, 0.3);

  private static final double dotRadius = 0.03;

  /// How far the crosshair ray may pass a dot's center and still hover it. Slightly larger than
  /// the dot itself so that small dots stay easy to aim at.
  private static final double hoverRadius = 0.05;

  /// Vertical speed the ladder climb assist forces: (0.2 - gravity 0.08) * drag 0.98.
  private static final double ladderAssistSpeed = 0.1176;
  private static final double assistEpsilon = 0.005;

  private static final int tooltipPadding = 3;

  private final ParkourDisplayAddon addon;

  /// The index of the dot under the crosshair, resolved every frame during world rendering: 0 for
  /// the start position, the 1-based run tick otherwise and -1 if nothing is hovered.
  private int hoveredIndex = -1;

  @Nullable
  private Position hoveredPosition;

  /// Positions of the ticks that show their hitbox instead of a dot. Keyed by position so that
  /// they stay toggled when the run is repeated exactly, e.g. by a macro.
  private final Set<Position> toggled = new HashSet<>();

  @Subscribe
  public void onInteract(ClientPlayerInteractEvent event) {
    var position = this.hoveredPosition;

    if (position == null) {
      return;
    }

    // Holding attack must not mine the block behind the hovered tick either.
    if (event.type() == InteractionType.ATTACK) {
      if (!this.toggled.remove(position)) {
        this.toggled.add(position);
      }

      event.setCancelled(true);
    } else if (event.type() == InteractionType.CONTINUE_ATTACK) {
      event.setCancelled(true);
    }
  }

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    this.hoveredIndex = -1;
    this.hoveredPosition = null;

    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    var start = this.startPosition();

    if (player == null || start == null) {
      return;
    }

    var ticks = this.addon.runState().previousTickStates();

    // The actual camera position rather than the render position: on old versions the latter is
    // the player's feet, which would cast the ray from the trajectory's own height.
    var eye = event.camera().position();
    // The player's rotation rather than the camera's: the camera derives yaw and pitch from its
    // quaternion in a different convention (pitch is positive looking up), which mirrors the
    // ray. In back third person the crosshair ray is parallel to the player's look anyway.
    var yaw = Math.toRadians(player.getRotationYaw());
    var pitch = Math.toRadians(player.getRotationPitch());
    var ray = new Ray(
        eye,
        -Math.sin(yaw) * Math.cos(pitch),
        -Math.sin(pitch),
        Math.cos(yaw) * Math.cos(pitch)
    );

    // Dots are small, so a dot whose center passes within the hover radius wins over any hitbox
    // the ray enters. Among dots the one closest to the ray wins, so that neighbouring dots don't
    // flicker between each other, among hitboxes the one entered first.
    var hover = new Hover();
    this.checkHover(hover, 0, start, playerAABB.move(start.toVector()), ray);

    for (var i = 0; i < ticks.size(); i++) {
      var position = ticks.get(i).position();
      this.checkHover(
          hover, i + 1, Position.of(position.toVector()), position.playerBoundingBox(), ray);
    }

    if (hover.dotIndex >= 0) {
      this.hoveredIndex = hover.dotIndex;
      this.hoveredPosition = hover.dotPosition;
    } else {
      this.hoveredIndex = hover.hitboxIndex;
      this.hoveredPosition = hover.hitboxPosition;
    }

    var renderPosition = event.camera().renderPosition();
    this.renderStart(event, renderPosition, start);
    this.renderTicks(event, renderPosition, ticks);
  }

  private void checkHover(
      Hover hover, int index, Position position, AxisAlignedBoundingBox hitbox, Ray ray) {
    if (this.toggled.contains(position)) {
      var distance = ray.boxDistance(hitbox);

      if (distance < hover.hitboxDistance) {
        hover.hitboxDistance = distance;
        hover.hitboxIndex = index;
        hover.hitboxPosition = position;
      }
    } else {
      var distance = ray.pointDistance(position);

      if (distance < hover.dotDistance) {
        hover.dotDistance = distance;
        hover.dotIndex = index;
        hover.dotPosition = position;
      }
    }
  }

  @Subscribe
  public void onRenderOverlay(IngameOverlayRenderEvent event) {
    var index = this.hoveredIndex;
    var ticks = this.addon.runState().previousTickStates();

    if (event.phase() != Phase.POST || event.isGuiHidden()
        || index < 0 || index > ticks.size() || this.hoveredPosition == null) {
      return;
    }

    var lines = index == 0
        ? this.startLines(this.hoveredPosition)
        : this.tickLines(index, ticks.get(index - 1));

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

  private List<RenderableComponent> startLines(Position start) {
    var format = this.addon.configuration().decimalFormat();
    var lines = new ArrayList<RenderableComponent>();
    lines.add(RenderableComponent.of(Component.text("Start", NamedTextColor.WHITE)));
    lines.add(line("Y", format.formatted(start.y()), NamedTextColor.AQUA));
    lines.add(line("X / Z",
        format.formatted(start.x()) + " / " + format.formatted(start.z()),
        NamedTextColor.AQUA));
    return lines;
  }

  private List<RenderableComponent> tickLines(int runTick, RunTickState tick) {
    var format = this.addon.configuration().decimalFormat();
    var vyColor = tick.vy() > 0
        ? NamedTextColor.GREEN
        : tick.vy() < 0 ? NamedTextColor.RED : NamedTextColor.GRAY;

    var position = tick.position();
    var flags = new StringBuilder();
    if (position.onClimbable()) flags.append("[onLadder] ");
    if (position.onGround()) flags.append("[onGround] ");
    if (position.collided()) flags.append("[collided] ");

    var lines = new ArrayList<RenderableComponent>();
    lines.add(line("Tick", String.valueOf(runTick), NamedTextColor.WHITE));
    lines.add(line("Airtime", String.valueOf(tick.airTime()), NamedTextColor.WHITE));
    lines.add(line("Y", format.formatted(position.y()), NamedTextColor.AQUA));
    lines.add(line("vy", format.formatted(tick.vy()), vyColor));
    lines.add(line("X / Z",
        format.formatted(position.x()) + " / " + format.formatted(position.z()),
        NamedTextColor.AQUA));
    lines.add(line("vx / vz",
        format.formatted(tick.vx()) + " / " + format.formatted(tick.vz()), NamedTextColor.GRAY));
    lines.add(line("Yaw", format.formatted(position.yaw()), NamedTextColor.GRAY));
    lines.add(line("Inputs", tick.input().formatted(), NamedTextColor.YELLOW));

    if (!flags.isEmpty()) {
      lines.add(RenderableComponent.of(
          Component.text(flags.toString().trim(), NamedTextColor.BLUE)));
    }

    return lines;
  }

  /// The start position while the previous run's ticks are shown, otherwise null.
  @Nullable
  private Position startPosition() {
    var settings = this.addon.configuration().highlightRunTickStates();
    var run = this.addon.runState();

    if (!settings.enabled().get() || run.startPosition() == null || run.endSplit() == null
        || run.previousTickStates().isEmpty()) {
      return null;
    }

    return Position.of(run.startPosition());
  }

  private void renderStart(RenderWorldEvent event, DoubleVector3 renderPosition, Position start) {
    var settings = this.addon.configuration().highlightRunTickStates();
    var isHovered = this.hoveredIndex == 0;

    if (this.toggled.contains(start)) {
      this.renderHitbox(
          event,
          renderPosition,
          playerAABB.move(start.toVector()),
          settings.startFillColor().get(),
          isHovered ? Color.WHITE : settings.startOutlineColor().get()
      );
      return;
    }

    this.renderDot(
        event,
        renderPosition,
        start,
        isHovered ? Color.WHITE : settings.startOutlineColor().get().withAlpha(255),
        isHovered
    );
  }

  private void renderTicks(
      RenderWorldEvent event, DoubleVector3 renderPosition, List<RunTickState> ticks) {
    var settings = this.addon.configuration().highlightRunTickStates();

    for (var i = 0; i < ticks.size(); i++) {
      var tick = ticks.get(i);
      var position = tick.position();
      var isHovered = this.hoveredIndex == i + 1;

      if (this.toggled.contains(Position.of(position.toVector()))) {
        var fillColor = position.onClimbable()
            ? Color.PINK
            : position.onGround()
                ? settings.onGroundFillColor().get()
                : settings.regularFillColor().get();
        var outlineColor = isHovered
            ? Color.WHITE
            : position.onGround()
                ? settings.onGroundOutlineColor().get()
                : settings.regularOutlineColor().get();

        this.renderHitbox(
            event, renderPosition, position.playerBoundingBox(), fillColor, outlineColor);
        continue;
      }

      this.renderDot(
          event,
          renderPosition,
          Position.of(position.toVector()),
          isHovered ? Color.WHITE : dotColor(tick),
          isHovered
      );
    }
  }

  private void renderHitbox(
      RenderWorldEvent event,
      DoubleVector3 renderPosition,
      AxisAlignedBoundingBox hitbox,
      Color fillColor,
      Color outlineColor
  ) {
    RenderUtils.renderAbsoluteBoundingBox(
        renderPosition,
        hitbox,
        this.addon.configuration().highlightRunTickStates().outlineThickness().get() / 1000F,
        event.stack(),
        fillColor.get(),
        outlineColor.get()
    );
  }

  private void renderDot(
      RenderWorldEvent event,
      DoubleVector3 renderPosition,
      Position position,
      Color color,
      boolean isHovered
  ) {
    var radius = isHovered ? dotRadius * 1.5 : dotRadius;

    RenderUtils.renderAbsoluteBoundingBox(
        renderPosition,
        new AxisAlignedBoundingBox(
            position.x() - radius, position.y() - radius, position.z() - radius,
            position.x() + radius, position.y() + radius, position.z() + radius
        ),
        0.005F,
        event.stack(),
        color.withAlpha(120).get(),
        color.get()
    );
  }

  private static RenderableComponent line(String label, String value, TextColor valueColor) {
    return RenderableComponent.of(
        Component.text(label + ": ", NamedTextColor.GRAY)
            .append(Component.text(value, valueColor))
    );
  }

  /// Colored like the jump graph: cyan while the climb assist drives the tick, yellow on any
  /// other climbable tick, otherwise green when rising and red when falling.
  private static Color dotColor(RunTickState tick) {
    if (tick.position().onClimbable()) {
      return Math.abs(tick.vy() - ladderAssistSpeed) < assistEpsilon
          ? Color.CYAN
          : Color.YELLOW;
    }

    if (tick.vy() > 0) {
      return Color.GREEN;
    }

    return tick.vy() < 0 ? Color.RED : Color.GRAY;
  }

  private record Position(double x, double y, double z) {

    static Position of(DoubleVector3 vector) {
      return new Position(vector.getX(), vector.getY(), vector.getZ());
    }

    DoubleVector3 toVector() {
      return new DoubleVector3(this.x, this.y, this.z);
    }
  }

  /// The crosshair ray, starting at the camera with a normalized direction.
  private record Ray(DoubleVector3 origin, double dirX, double dirY, double dirZ) {

    /// The distance between a point and the ray, or infinity if it lies behind the camera.
    double pointDistance(Position point) {
      var toX = point.x() - this.origin.getX();
      var toY = point.y() - this.origin.getY();
      var toZ = point.z() - this.origin.getZ();
      var along = toX * this.dirX + toY * this.dirY + toZ * this.dirZ;

      if (along <= 0) {
        return Double.POSITIVE_INFINITY;
      }

      var offX = toX - along * this.dirX;
      var offY = toY - along * this.dirY;
      var offZ = toZ - along * this.dirZ;
      return Math.sqrt(offX * offX + offY * offY + offZ * offZ);
    }

    /// The distance along the ray at which it enters the box, or infinity if it misses the box or
    /// the box lies behind the camera.
    double boxDistance(AxisAlignedBoundingBox box) {
      var near = Double.NEGATIVE_INFINITY;
      var far = Double.POSITIVE_INFINITY;
      double[][] slabs = {
          {this.origin.getX(), this.dirX, box.getMinX(), box.getMaxX()},
          {this.origin.getY(), this.dirY, box.getMinY(), box.getMaxY()},
          {this.origin.getZ(), this.dirZ, box.getMinZ(), box.getMaxZ()},
      };

      for (var slab : slabs) {
        var origin = slab[0];
        var dir = slab[1];

        if (dir == 0) {
          if (origin < slab[2] || origin > slab[3]) {
            return Double.POSITIVE_INFINITY;
          }
          continue;
        }

        var t1 = (slab[2] - origin) / dir;
        var t2 = (slab[3] - origin) / dir;
        near = Math.max(near, Math.min(t1, t2));
        far = Math.min(far, Math.max(t1, t2));
      }

      if (near > far || far < 0) {
        return Double.POSITIVE_INFINITY;
      }

      // Standing inside a hitbox still hovers it.
      return Math.max(near, 0);
    }
  }

  /// The best dot and hitbox candidates while resolving the hovered tick.
  private static final class Hover {

    int dotIndex = -1;
    @Nullable Position dotPosition;
    double dotDistance = hoverRadius;

    int hitboxIndex = -1;
    @Nullable Position hitboxPosition;
    double hitboxDistance = Double.POSITIVE_INFINITY;
  }
}
