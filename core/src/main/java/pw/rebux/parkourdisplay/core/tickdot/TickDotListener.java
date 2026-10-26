package pw.rebux.parkourdisplay.core.tickdot;

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
import pw.rebux.parkourdisplay.core.run.RunTickState;
import pw.rebux.parkourdisplay.core.util.RenderUtils;

/// Renders the ticks of the previous run as dots in the world, colored by their vertical movement
/// and ladder state. Looking at a dot shows the state of that tick next to the crosshair, hitting
/// it toggles between the dot and the tick's hitbox.
@RequiredArgsConstructor
public final class TickDotListener {

  private static final double dotRadius = 0.03;

  /// How far the crosshair ray may pass a dot's center and still hover it. Slightly larger than
  /// the dot itself so that small dots stay easy to aim at.
  private static final double hoverRadius = 0.05;

  /// Vertical speed the ladder climb assist forces: (0.2 - gravity 0.08) * drag 0.98.
  private static final double ladderAssistSpeed = 0.1176;
  private static final double assistEpsilon = 0.005;

  private static final int tooltipPadding = 3;

  private final ParkourDisplayAddon addon;

  /// The dot under the crosshair, resolved every frame during world rendering.
  @Nullable
  private TickDot hovered;

  /// The 1-based index of the hovered dot within the previous run.
  private int hoveredRunTick;

  /// Positions of the ticks that show their hitbox instead of a dot. Keyed by position so that
  /// they stay toggled when the run is repeated exactly, e.g. by a macro.
  private final Set<Position> toggled = new HashSet<>();

  @Subscribe
  public void onInteract(ClientPlayerInteractEvent event) {
    var dot = this.hovered;

    if (dot == null) {
      return;
    }

    // Holding attack must not mine the block behind the hovered tick either.
    if (event.type() == InteractionType.ATTACK) {
      var position = Position.of(dot);

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
    this.hovered = null;
    this.hoveredRunTick = 0;

    var player = this.addon.labyAPI().minecraft().getClientPlayer();

    if (player == null) {
      return;
    }

    var dots = this.runDots();

    if (dots.isEmpty()) {
      return;
    }

    // The actual camera position rather than the render position: on old versions the latter is
    // the player's feet, which would cast the ray from the trajectory's own height.
    var eye = event.camera().position();
    // The player's rotation rather than the camera's: the camera derives yaw and pitch from its
    // quaternion in a different convention (pitch is positive looking up), which mirrors the
    // ray. In back third person the crosshair ray is parallel to the player's look anyway.
    var yaw = Math.toRadians(player.getRotationYaw());
    var pitch = Math.toRadians(player.getRotationPitch());
    var dirX = -Math.sin(yaw) * Math.cos(pitch);
    var dirY = -Math.sin(pitch);
    var dirZ = Math.cos(yaw) * Math.cos(pitch);

    // Dots are small, so a dot whose center passes within the hover radius wins over any hitbox
    // the ray enters. Among dots the one closest to the ray wins, so that neighbouring dots don't
    // flicker between each other, among hitboxes the one entered first.
    var hover = new Hover();

    for (var i = 0; i < dots.size(); i++) {
      this.checkHover(hover, dots.get(i), i + 1, eye, dirX, dirY, dirZ);
    }

    if (hover.dot != null) {
      this.hovered = hover.dot;
      this.hoveredRunTick = hover.dotRunTick;
    } else {
      this.hovered = hover.hitbox;
      this.hoveredRunTick = hover.hitboxRunTick;
    }

    var renderPosition = event.camera().renderPosition();
    this.renderDots(event, renderPosition, dots);
  }

  private void checkHover(
      Hover hover, TickDot dot, int runTick,
      DoubleVector3 eye, double dirX, double dirY, double dirZ
  ) {
    if (this.toggled.contains(Position.of(dot))) {
      var distance = rayBoxDistance(dot.hitbox(), eye, dirX, dirY, dirZ);

      if (distance < hover.hitboxDistance) {
        hover.hitboxDistance = distance;
        hover.hitbox = dot;
        hover.hitboxRunTick = runTick;
      }
    } else {
      var distance = rayDistance(dot, eye, dirX, dirY, dirZ);

      if (distance < hover.dotDistance) {
        hover.dotDistance = distance;
        hover.dot = dot;
        hover.dotRunTick = runTick;
      }
    }
  }

  @Subscribe
  public void onRenderOverlay(IngameOverlayRenderEvent event) {
    var dot = this.hovered;

    if (event.phase() != Phase.POST || event.isGuiHidden() || dot == null) {
      return;
    }

    var format = this.addon.configuration().decimalFormat();
    var vyColor = dot.vy() > 0
        ? NamedTextColor.GREEN
        : dot.vy() < 0 ? NamedTextColor.RED : NamedTextColor.GRAY;

    var flags = new StringBuilder();
    if (dot.onClimbable()) flags.append("[onLadder] ");
    if (dot.onGround()) flags.append("[onGround] ");
    if (dot.collided()) flags.append("[collided] ");

    var lines = new ArrayList<RenderableComponent>();
    lines.add(line("Run Tick", String.valueOf(this.hoveredRunTick), NamedTextColor.WHITE));
    lines.add(line("Air Time", String.valueOf(dot.airTime()), NamedTextColor.WHITE));
    lines.add(line("Y", format.formatted(dot.y()), NamedTextColor.AQUA));
    lines.add(line("vY", format.formatted(dot.vy()), vyColor));
    lines.add(line("X / Z",
        format.formatted(dot.x()) + " / " + format.formatted(dot.z()), NamedTextColor.AQUA));
    lines.add(line("vX / vZ",
        format.formatted(dot.vx()) + " / " + format.formatted(dot.vz()), NamedTextColor.GRAY));
    lines.add(line("Yaw", format.formatted(dot.yaw()), NamedTextColor.GRAY));
    lines.add(line("Input", dot.inputs(), NamedTextColor.YELLOW));

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

  private List<TickDot> runDots() {
    var settings = this.addon.configuration().highlightRunTickStates();
    var run = this.addon.runState();

    if (!settings.enabled().get() || run.startPosition() == null || run.endSplit() == null) {
      return List.of();
    }

    return run.previousTickStates().stream().map(RunTickState::dot).toList();
  }

  private void renderDots(RenderWorldEvent event, DoubleVector3 renderPosition, List<TickDot> dots) {
    var settings = this.addon.configuration().highlightRunTickStates();

    for (var dot : dots) {
      var isHovered = dot == this.hovered;

      if (this.toggled.contains(Position.of(dot))) {
        var fillColor = dot.onClimbable()
            ? Color.PINK
            : dot.onGround() ? settings.onGroundFillColor().get() : settings.regularFillColor().get();
        var outlineColor = isHovered
            ? Color.WHITE
            : dot.onGround()
                ? settings.onGroundOutlineColor().get()
                : settings.regularOutlineColor().get();

        RenderUtils.renderAbsoluteBoundingBox(
            renderPosition,
            dot.hitbox(),
            settings.outlineThickness().get() / 1000F,
            event.stack(),
            fillColor.get(),
            outlineColor.get()
        );
        continue;
      }

      var color = isHovered ? Color.WHITE : dotColor(dot);
      var radius = isHovered ? dotRadius * 1.5 : dotRadius;

      RenderUtils.renderAbsoluteBoundingBox(
          renderPosition,
          new AxisAlignedBoundingBox(
              dot.x() - radius, dot.y() - radius, dot.z() - radius,
              dot.x() + radius, dot.y() + radius, dot.z() + radius
          ),
          0.005F,
          event.stack(),
          color.withAlpha(120).get(),
          color.get()
      );
    }
  }

  /// The distance between a dot's center and the crosshair ray, or infinity if the dot lies
  /// behind the camera.
  private static double rayDistance(
      TickDot dot, DoubleVector3 eye, double dirX, double dirY, double dirZ) {
    var toX = dot.x() - eye.getX();
    var toY = dot.y() - eye.getY();
    var toZ = dot.z() - eye.getZ();
    var along = toX * dirX + toY * dirY + toZ * dirZ;

    if (along <= 0) {
      return Double.POSITIVE_INFINITY;
    }

    var offX = toX - along * dirX;
    var offY = toY - along * dirY;
    var offZ = toZ - along * dirZ;
    return Math.sqrt(offX * offX + offY * offY + offZ * offZ);
  }

  /// The distance along the crosshair ray at which it enters the box, or infinity if it misses
  /// the box or the box lies behind the camera.
  private static double rayBoxDistance(
      AxisAlignedBoundingBox box, DoubleVector3 eye, double dirX, double dirY, double dirZ) {
    var near = Double.NEGATIVE_INFINITY;
    var far = Double.POSITIVE_INFINITY;
    double[][] slabs = {
        {eye.getX(), dirX, box.getMinX(), box.getMaxX()},
        {eye.getY(), dirY, box.getMinY(), box.getMaxY()},
        {eye.getZ(), dirZ, box.getMinZ(), box.getMaxZ()},
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

  private static RenderableComponent line(String label, String value, TextColor valueColor) {
    return RenderableComponent.of(
        Component.text(label + ": ", NamedTextColor.GRAY)
            .append(Component.text(value, valueColor))
    );
  }

  /// Colored like the jump graph: cyan while the climb assist drives the tick, yellow on any
  /// other climbable tick, otherwise green when rising and red when falling.
  private static Color dotColor(TickDot dot) {
    if (dot.onClimbable()) {
      return Math.abs(dot.vy() - ladderAssistSpeed) < assistEpsilon
          ? Color.CYAN
          : Color.YELLOW;
    }

    if (dot.vy() > 0) {
      return Color.GREEN;
    }

    return dot.vy() < 0 ? Color.RED : Color.GRAY;
  }

  private record Position(double x, double y, double z) {

    static Position of(TickDot dot) {
      return new Position(dot.x(), dot.y(), dot.z());
    }
  }

  /// The best dot and hitbox candidates while resolving the hovered tick.
  private static final class Hover {

    @Nullable TickDot dot;
    int dotRunTick;
    double dotDistance = hoverRadius;

    @Nullable TickDot hitbox;
    int hitboxRunTick;
    double hitboxDistance = Double.POSITIVE_INFINITY;
  }
}
