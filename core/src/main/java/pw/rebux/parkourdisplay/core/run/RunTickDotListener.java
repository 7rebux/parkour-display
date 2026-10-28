package pw.rebux.parkourdisplay.core.run;

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
import pw.rebux.parkourdisplay.core.util.BlockRayTracer;
import pw.rebux.parkourdisplay.core.util.MathHelper;
import pw.rebux.parkourdisplay.core.util.RenderUtils;

/// Renders the ticks of the previous run as dots in the world.
/// Looking at a dot shows the state of that tick next to the crosshair.
@RequiredArgsConstructor
public final class RunTickDotListener {

  private static final double DOT_RADIUS = 0.03;
  private static final double HOVER_RADIUS = 0.05;
  private static final double HOVER_RANGE = 5;
  private static final int TOOLTIP_PADDING = 3;
  private static final int TOOLTIP_LINE_SPACING = 1;
  private static final double EPSILON = 1e-9;
  private static final double LADDER_CLIMB_SPEED = (0.2 - 0.08) * 0.98F;

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

    // The run can be cleared between the world and the overlay render.
    if (event.phase() != Phase.POST || event.isGuiHidden() || index < 0 || index >= ticks.size()) {
      return;
    }

    var lines = this.tickLines(index + 1, ticks.get(index));

    var width = 0F;
    var height = 0F;

    for (var line : lines) {
      width = Math.max(width, line.getWidth());
      height += line.getHeight() + TOOLTIP_LINE_SPACING;
    }

    height -= TOOLTIP_LINE_SPACING;

    var window = this.addon.labyAPI().minecraft().minecraftWindow();
    // Offset from the crosshair so the tooltip never covers the dot being inspected.
    var x = window.getScaledWidth() / 2F + 8;
    var y = window.getScaledHeight() / 2F + 8;
    var canvas = event.context().canvas();

    canvas.submitRect(
        Rectangle.relative(x, y, width + TOOLTIP_PADDING * 2, height + TOOLTIP_PADDING * 2),
        0xA0000000
    );

    var lineY = y + TOOLTIP_PADDING;

    for (var line : lines) {
      canvas.submitRenderableComponent(line, x + TOOLTIP_PADDING, lineY, -1, TextRenderingOptions.SHADOW);
      lineY += line.getHeight() + TOOLTIP_LINE_SPACING;
    }
  }

  private boolean isHidden() {
    var run = this.addon.runState();

    return !this.addon.configuration().highlightRunTickStates().enabled().get()
        || run.startPosition() == null
        || run.endSplit() == null
        || run.previousTickStates().isEmpty();
  }

  private boolean isHitboxesMode() {
    return this.addon.configuration().highlightRunTickStates().renderMode().get()
        == RunTickStateRenderMode.Hitboxes;
  }

  private void updateHoveredIndex(RenderWorldEvent event, ClientPlayer player, List<RunTickState> ticks) {
    var origin = event.camera().position();
    var direction = new DoubleVector3(player.perspectiveVector(event.getPartialTicks()));

    // Dots: the one closest to the ray wins, so neighbouring dots don't flicker between each other.
    // Hitboxes: the one the ray enters first wins. Misses are NaN, which never compares as closer.
    var hitboxes = this.isHitboxesMode();
    var closest = hitboxes ? Double.POSITIVE_INFINITY : HOVER_RADIUS;

    for (var i = 0; i < ticks.size(); i++) {
      var position = ticks.get(i).position();
      var distance = hitboxes
          ? BlockRayTracer.intersect(origin, direction, HOVER_RANGE, position.playerBoundingBox())
          : MathHelper.rayPointDistance(origin, direction, HOVER_RANGE, position.toVector());

      if (distance < closest) {
        closest = distance;
        this.hoveredIndex = i;
      }
    }
  }

  private List<RenderableComponent> tickLines(int runTick, RunTickState tick) {
    var position = tick.position();

    return List.of(
        RenderableComponent.of(Component.text("Tick " + runTick, NamedTextColor.WHITE)),
        line(
            "Position",
            this.number(position.x()),
            this.number(position.y()),
            this.number(position.z())
        ),
        line(
            "Rotation",
            this.number(MathHelper.formatYaw(position.yaw())),
            this.number(position.pitch())
        ),
        line(
            "Velocity",
            this.number(position.vx()),
            this.number(position.vy(), bySign(position.vy(), NamedTextColor.GRAY, NamedTextColor.GREEN, NamedTextColor.RED)),
            this.number(position.vz())
        ),
        line("onClimbable", position.onClimbable()),
        line("onGround", position.onGround()),
        line("collided", position.collided()),
        line("Inputs", Component.text(formatInputState(tick.input()), NamedTextColor.YELLOW))
    );
  }

  private void renderTicks(RenderWorldEvent event, List<RunTickState> ticks) {
    var renderPosition = event.camera().renderPosition();
    var hitboxes = this.isHitboxesMode();

    for (var i = 0; i < ticks.size(); i++) {
      var tick = ticks.get(i);
      var position = tick.position();
      var isHovered = this.hoveredIndex == i;
      var color = isHovered ? Color.WHITE : tickColor(tick);

      AxisAlignedBoundingBox box;
      float outlineThickness;
      int fillAlpha;

      if (hitboxes) {
        box = position.playerBoundingBox();
        outlineThickness = 0.005F;
        fillAlpha = 30;
      } else {
        var radius = isHovered ? DOT_RADIUS * 1.5 : DOT_RADIUS;
        box = new AxisAlignedBoundingBox(
            position.x() - radius, position.y() - radius, position.z() - radius,
            position.x() + radius, position.y() + radius, position.z() + radius
        );
        outlineThickness = 0.002F;
        fillAlpha = 120;
      }

      RenderUtils.renderAbsoluteBoundingBox(
          renderPosition,
          box,
          outlineThickness,
          event.stack(),
          color.withAlpha(fillAlpha).get(),
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

  private static <T> T bySign(double value, T zero, T positive, T negative) {
    if (Math.abs(value) < EPSILON) {
      return zero;
    }
    return value > 0 ? positive : negative;
  }

  private static Color tickColor(RunTickState tick) {
    var position = tick.position();

    if (Math.abs(position.vy() - LADDER_CLIMB_SPEED) < EPSILON) {
      return Color.CYAN;
    }
    if (position.onGround()) {
      return Color.YELLOW;
    }
    return bySign(position.vy(), Color.GRAY, Color.GREEN, Color.RED);
  }

  private static String formatInputState(RunTickState.KeyboardInput state) {
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
}
