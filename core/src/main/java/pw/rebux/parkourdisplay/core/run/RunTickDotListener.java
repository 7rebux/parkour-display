package pw.rebux.parkourdisplay.core.run;

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
import net.labymod.api.event.client.render.overlay.IngameOverlayRenderEvent;
import net.labymod.api.event.client.render.world.RenderWorldEvent;
import net.labymod.api.util.Color;
import net.labymod.api.util.bounds.Rectangle;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.ParkourDisplayConfiguration.RunTickStateRenderMode;
import pw.rebux.parkourdisplay.core.util.MathHelper;
import pw.rebux.parkourdisplay.core.util.RenderUtils;

/// Renders the ticks of the previous run as dots in the world.
/// Looking at a dot shows the state of that tick next to the crosshair.
@RequiredArgsConstructor
public final class RunTickDotListener {

  private static final double dotRadius = 0.03;
  private static final double hoverRadius = 0.05;
  private static final int tooltipPadding = 3;
  private static final double epsilon = 1e-9;
  private static final double ladderClimbSpeed = (0.2 - 0.08) * 0.98F;

  private final ParkourDisplayAddon addon;

  private int hoveredIndex = -1;

  @Subscribe
  public void onRenderWorld(RenderWorldEvent event) {
    this.hoveredIndex = -1;

    var player = this.addon.labyAPI().minecraft().getClientPlayer();
    if (player == null || !this.visible()) {
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

    // The dot closest to the ray wins, so that neighbouring dots don't flicker between each other,
    // or the hitbox the ray enters first.
    var hitboxes = this.hitboxes();
    var closest = hitboxes ? Double.POSITIVE_INFINITY : hoverRadius;

    for (var i = 0; i < ticks.size(); i++) {
      var position = ticks.get(i).position();
      var distance = hitboxes
          ? ray.boxDistance(position.playerBoundingBox())
          : ray.pointDistance(position.toVector());

      if (distance < closest) {
        closest = distance;
        this.hoveredIndex = i;
      }
    }

    var renderPosition = event.camera().renderPosition();
    this.renderTicks(event, renderPosition, ticks, hitboxes);
  }

  @Subscribe
  public void onRenderOverlay(IngameOverlayRenderEvent event) {
    var index = this.hoveredIndex;
    var ticks = this.addon.runState().previousTickStates();

    if (event.phase() != Phase.POST || event.isGuiHidden()
        || index < 0 || index >= ticks.size() || !this.visible()) {
      return;
    }

    var lines = this.tickLines(index + 1, ticks.get(index));

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

  private List<RenderableComponent> tickLines(int runTick, RunTickState tick) {
    var format = this.addon.configuration().decimalFormat();
    var position = tick.position();
    var title = isClimbTick(position.vy())
        ? Component.text("Tick " + runTick + " (Climb)", NamedTextColor.AQUA)
        : Component.text("Tick " + runTick, NamedTextColor.WHITE);

    var lines = new ArrayList<RenderableComponent>();
    lines.add(RenderableComponent.of(title));
    lines.add(line("Position", format.formatted(position.x()) + " / " + format.formatted(position.y()) + " / " + format.formatted(position.z()), NamedTextColor.WHITE));
    lines.add(line("Rotation", format.formatted(MathHelper.formatYaw(position.yaw())) + " / " + format.formatted(position.pitch()), NamedTextColor.WHITE));
    lines.add(RenderableComponent.of(
        Component.text("Velocity: ", NamedTextColor.GRAY)
            .append(Component.text(format.formatted(position.vx()), NamedTextColor.WHITE))
            .append(Component.text(" / ", NamedTextColor.GRAY))
            .append(Component.text(format.formatted(position.vy()), signColor(position.vy())))
            .append(Component.text(" / ", NamedTextColor.GRAY))
            .append(Component.text(format.formatted(position.vz()), NamedTextColor.WHITE))
    ));
    lines.add(line("Speed", format.formatted(Math.hypot(position.vx(), position.vz())), NamedTextColor.WHITE));
    lines.add(line("onClimbable", position.onClimbable()));
    lines.add(line("onGround", position.onGround()));
    lines.add(line("collided", position.collided()));
    lines.add(line("Inputs", formatInputState(tick.input()), NamedTextColor.YELLOW));

    return lines;
  }

  private String formatInputState(RunTickState.KeyboardInput state) {
    var input = new StringBuilder();

    if (state.w()) input.append("W");
    if (state.a()) input.append("A");
    if (state.s()) input.append("S");
    if (state.d()) input.append("D");
    if (state.jump()) input.append(input.isEmpty() ? "" : " ").append("Jump");
    if (state.sneak()) input.append(input.isEmpty() ? "" : " ").append("Sneak");
    if (state.sprint()) input.append(input.isEmpty() ? "" : " ").append("Sprint");

    return input.isEmpty() ? "-" : input.toString();
  }

  private boolean visible() {
    var run = this.addon.runState();

    return this.addon.configuration().highlightRunTickStates().enabled().get()
        && run.startPosition() != null && run.endSplit() != null
        && !run.previousTickStates().isEmpty();
  }

  private boolean hitboxes() {
    return this.addon.configuration().highlightRunTickStates().renderMode().get()
        == RunTickStateRenderMode.Hitboxes;
  }

  private void renderTicks(
      RenderWorldEvent event,
      DoubleVector3 renderPosition,
      List<RunTickState> ticks,
      boolean hitboxes
  ) {
    for (var i = 0; i < ticks.size(); i++) {
      var tick = ticks.get(i);
      var position = tick.position();
      var isHovered = this.hoveredIndex == i;

      if (hitboxes) {
        var color = dotColor(tick);

        this.renderHitbox(
            event,
            renderPosition,
            position.playerBoundingBox(),
            color.withAlpha(30),
            isHovered ? Color.WHITE : color
        );
        continue;
      }

      this.renderDot(
          event,
          renderPosition,
          position.toVector(),
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
        0.005F,
        event.stack(),
        fillColor.get(),
        outlineColor.get()
    );
  }

  private void renderDot(
      RenderWorldEvent event,
      DoubleVector3 renderPosition,
      DoubleVector3 position,
      Color color,
      boolean isHovered
  ) {
    var radius = isHovered ? dotRadius * 1.5 : dotRadius;

    RenderUtils.renderAbsoluteBoundingBox(
        renderPosition,
        new AxisAlignedBoundingBox(
            position.getX() - radius, position.getY() - radius, position.getZ() - radius,
            position.getX() + radius, position.getY() + radius, position.getZ() + radius
        ),
        0.002F,
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

  private static RenderableComponent line(String label, boolean value) {
    return line(label, String.valueOf(value), value ? NamedTextColor.GREEN : NamedTextColor.RED);
  }

  private static TextColor signColor(double value) {
    if (Math.abs(value) < epsilon) {
      return NamedTextColor.GRAY;
    }

    return value > 0 ? NamedTextColor.GREEN : NamedTextColor.RED;
  }

  private static Color dotColor(RunTickState tick) {
    var position = tick.position();
    if (isClimbTick(position.vy())) {
      return Color.CYAN;
    }

    if (Math.abs(position.vy()) < epsilon) {
      return Color.GRAY;
    }

    return position.vy() > 0 ? Color.GREEN : Color.RED;
  }

  private static boolean isClimbTick(double vy) {
    return Math.abs(vy - ladderClimbSpeed) < epsilon;
  }

  /// The crosshair ray, starting at the camera with a normalized direction.
  private record Ray(DoubleVector3 origin, double dirX, double dirY, double dirZ) {

    /// The distance between a point and the ray, or infinity if it lies behind the camera.
    double pointDistance(DoubleVector3 point) {
      var toX = point.getX() - this.origin.getX();
      var toY = point.getY() - this.origin.getY();
      var toZ = point.getZ() - this.origin.getZ();
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
}
