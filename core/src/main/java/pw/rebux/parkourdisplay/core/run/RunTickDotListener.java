package pw.rebux.parkourdisplay.core.run;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextColor;
import net.labymod.api.client.entity.player.ClientPlayer;
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
    if (player == null || this.isHidden()) {
      return;
    }

    var ticks = this.addon.runState().previousTickStates();

    if (this.addon.configuration().highlightRunTickStates().showTickInfoOnHover().get()) {
      this.updateHoveredIndex(event, player, ticks);
    }

    this.renderTicks(event, ticks);
  }

  @Subscribe
  public void onRenderOverlay(IngameOverlayRenderEvent event) {
    var index = this.hoveredIndex;
    var ticks = this.addon.runState().previousTickStates();

    if (event.phase() != Phase.POST || event.isGuiHidden()
        || index < 0 || index >= ticks.size() || this.isHidden()) {
      return;
    }

    var lines = this.tickLines(index + 1, ticks.get(index));

    var width = 0F;
    var height = 0F;

    for (var line : lines) {
      width = Math.max(width, line.getWidth());
      height += line.getHeight() + 1;
    }

    var window = this.addon.labyAPI().minecraft().minecraftWindow();
    // Offset from the crosshair so the tooltip never covers the dot being inspected.
    var x = window.getScaledWidth() / 2F + 8;
    var y = window.getScaledHeight() / 2F + 8;
    var canvas = event.context().canvas();

    canvas.submitRect(
        Rectangle.relative(x, y, width + tooltipPadding * 2, height + tooltipPadding * 2),
        0xA0000000
    );

    var lineY = y + tooltipPadding;

    for (var line : lines) {
      canvas.submitRenderableComponent(line, x + tooltipPadding, lineY, -1, TextRenderingOptions.SHADOW);
      lineY += line.getHeight() + 1;
    }
  }

  private boolean isHidden() {
    var run = this.addon.runState();

    return !this.addon.configuration().highlightRunTickStates().enabled().get()
        || run.startPosition() == null
        || run.endSplit() == null
        || run.previousTickStates().isEmpty();
  }

  private boolean hitboxes() {
    return this.addon.configuration().highlightRunTickStates().renderMode().get()
        == RunTickStateRenderMode.Hitboxes;
  }

  private void updateHoveredIndex(RenderWorldEvent event, ClientPlayer player, List<RunTickState> ticks) {
    var eye = event.camera().position();
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
  }

  private List<RenderableComponent> tickLines(int runTick, RunTickState tick) {
    var position = tick.position();
    var title = Component.text("Tick " + runTick, NamedTextColor.WHITE);

    var lines = new ArrayList<RenderableComponent>();
    lines.add(RenderableComponent.of(title));
    lines.add(line(
        "Position",
        this.number(position.x()),
        this.number(position.y()),
        this.number(position.z())
    ));
    lines.add(line(
        "Rotation",
        this.number(MathHelper.formatYaw(position.yaw())),
        this.number(position.pitch())
    ));
    lines.add(line(
        "Velocity",
        this.number(position.vx()),
        this.number(position.vy(), signColor(position.vy())),
        this.number(position.vz())
    ));
    lines.add(line("onClimbable", position.onClimbable()));
    lines.add(line("onGround", position.onGround()));
    lines.add(line("collided", position.collided()));
    lines.add(line("Inputs", Component.text(formatInputState(tick.input()), NamedTextColor.YELLOW)));

    return lines;
  }

  private void renderTicks(RenderWorldEvent event, List<RunTickState> ticks) {
    var renderPosition = event.camera().renderPosition();
    var hitboxes = this.hitboxes();

    for (var i = 0; i < ticks.size(); i++) {
      var tick = ticks.get(i);
      var position = tick.position();
      var isHovered = this.hoveredIndex == i;
      var color = isHovered ? Color.WHITE : tickColor(tick);

      if (hitboxes) {
        RenderUtils.renderAbsoluteBoundingBox(
            renderPosition,
            position.playerBoundingBox(),
            0.005F,
            event.stack(),
            color.withAlpha(30).get(),
            color.get()
        );
        continue;
      }

      var radius = isHovered ? dotRadius * 1.5 : dotRadius;

      RenderUtils.renderAbsoluteBoundingBox(
          renderPosition,
          new AxisAlignedBoundingBox(
              position.toVector().getX() - radius, position.toVector().getY() - radius, position.toVector().getZ() - radius,
              position.toVector().getX() + radius, position.toVector().getY() + radius, position.toVector().getZ() + radius
          ),
          0.002F,
          event.stack(),
          color.withAlpha(120).get(),
          color.get()
      );
    }
  }

  private Component number(double value) {
    return this.number(value, NamedTextColor.WHITE);
  }

  private Component number(double value, TextColor color) {
    return Component.text(this.addon.configuration().decimalFormat().formatted(value), color);
  }

  private static RenderableComponent line(String label, Component... values) {
    Component line = Component.text(label + ": ", NamedTextColor.GRAY);

    for (var i = 0; i < values.length; i++) {
      if (i > 0) {
        line = line.append(Component.text(" / ", NamedTextColor.GRAY));
      }

      line = line.append(values[i]);
    }

    return RenderableComponent.of(line);
  }

  private static RenderableComponent line(String label, boolean value) {
    return line(
        label,
        Component.text(String.valueOf(value), value ? NamedTextColor.GREEN : NamedTextColor.RED)
    );
  }

  private static TextColor signColor(double value) {
    if (Math.abs(value) < epsilon) {
      return NamedTextColor.GRAY;
    }

    return value > 0 ? NamedTextColor.GREEN : NamedTextColor.RED;
  }

  private static Color tickColor(RunTickState tick) {
    var position = tick.position();

    if (Math.abs(position.vy() - ladderClimbSpeed) < epsilon) {
      return Color.CYAN;
    }
    if (position.onGround()) {
      return Color.YELLOW;
    }
    if (Math.abs(position.vy()) < epsilon) {
      return Color.GRAY;
    }
    return position.vy() > 0 ? Color.GREEN : Color.RED;
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
