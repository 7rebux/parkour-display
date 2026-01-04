package pw.rebux.parkourdisplay.core.widget;

import java.util.Optional;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.gfx.pipeline.renderer.text.TextRenderingOptions;
import net.labymod.api.client.gui.hud.hudwidget.HudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.SimpleHudWidget;
import net.labymod.api.client.gui.hud.position.HudSize;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.color.ColorPickerWidget.ColorPickerSetting;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.util.Color;
import net.labymod.api.util.bounds.Rectangle;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.TickFormatter;
import pw.rebux.parkourdisplay.core.widget.RunSplitsWidget.RunSplitsWidgetConfig;

public final class RunSplitsWidget extends SimpleHudWidget<RunSplitsWidgetConfig> {

  private static final Component SPLITS_TITLE_COMPONENT =
      Component.text("SPLITS", NamedTextColor.YELLOW);

  private static final float WIDTH = 100;
  private static final float PADDING = 2;

  private final ParkourDisplayAddon addon;

  public RunSplitsWidget(ParkourDisplayAddon addon) {
    super("run_splits", RunSplitsWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void render(
      RenderPhase phase,
      ScreenContext context,
      boolean isEditorContext,
      HudSize size
  ) {
    var splits = this.addon.playerParkourState().runSplits();
    var endSplit = this.addon.playerParkourState().runEndSplit();

    // Title component
    var titleComponent = RenderableComponent.of(SPLITS_TITLE_COMPONENT);

    // Split components
    var splitRows = splits.stream()
        .map(split -> {
          // Label
          var label = RenderableComponent.of(Component.text("Label", NamedTextColor.WHITE));

          // Time
          var timeColor = split.passed()
              ? NamedTextColor.WHITE
              : NamedTextColor.GRAY;
          var timeText = split.passed()
              ? formatTicks(split.lastTicks())
              : split.personalBest() == null
                  ? "N/A"
                  : formatTicks(split.personalBest());
          var time = RenderableComponent.of(Component.text(timeText, timeColor));

          // Optional delta
          Optional<RenderableComponent> delta = Optional.empty();

          if (split.passed()) {
            long ticks = split.lastTicks() - split.personalBest();
            var color = ticks < 0
                ? NamedTextColor.GREEN
                : ticks > 0
                    ? NamedTextColor.RED
                    : NamedTextColor.GRAY;
            var text = (ticks >= 0 ? "+" : "") + Math.abs(ticks);

            delta = Optional.of(RenderableComponent.of(Component.text(text, color)));
          }

          return new SplitRow(label, delta, time);
        })
        .toList();

    // Timer component
    var timerTicks = this.addon.playerParkourState().runTimer();
    var timerColor = timerTicks <= endSplit.personalBest()
        ? NamedTextColor.GREEN
        : NamedTextColor.RED;
    var timerComponent = RenderableComponent.of(Component.text(formatTicks(timerTicks), timerColor));

    // Personal best component
    var personalBest = endSplit.personalBest() == null ? "N/A" : formatTicks(endSplit.personalBest());
    var personalBestComponent = RenderableComponent.of(Component.text(personalBest, NamedTextColor.WHITE));
    var personalBestLabelComponent = RenderableComponent.of(Component.text("Personal Best", NamedTextColor.WHITE));

    float yOffset = 0;

    if (phase.canRender()) {
      int backgroundRGBA = this.config.backgroundColor().get().get();
      float totalHeight = 0
          + titleComponent.getHeight()
          + (splits.size() * (titleComponent.getHeight() + 1))
          + titleComponent.getHeight()
          + timerComponent.getHeight()
          + personalBestComponent.getHeight()
          + (PADDING * 2);

      // Render background
      context.canvas().submitRect(Rectangle.relative(0, 0, WIDTH, totalHeight), backgroundRGBA);
      context.canvas().submitRect(Rectangle.relative(0, 0, WIDTH, titleComponent.getHeight() + 2), backgroundRGBA);

      // Render Title
      context.canvas().submitRenderableComponent(titleComponent, WIDTH / 2, PADDING, -1, TextRenderingOptions.CENTERED);
      yOffset += titleComponent.getHeight() + 2;

      // Render splits
      for (var splitRow : splitRows) {
        context.canvas().submitRenderableComponent(splitRow.label, PADDING, yOffset + PADDING, -1, TextRenderingOptions.NONE);
        context.canvas().submitRenderableComponent(splitRow.time, WIDTH - splitRow.time.getWidth() - PADDING, yOffset + PADDING, -1, TextRenderingOptions.NONE);

        if (splitRow.delta.isPresent()) {
          var deltaComponent = splitRow.delta.get();
          context.canvas().submitRenderableComponent(deltaComponent, WIDTH - splitRow.time.getWidth() - deltaComponent.getWidth() - 10, yOffset + PADDING, -1, TextRenderingOptions.NONE);
        }

        yOffset += splitRow.label.getHeight() + 1;
      }

      yOffset += titleComponent.getHeight();

      // Render timer
      context.canvas().submitRenderableComponent(timerComponent, WIDTH - timerComponent.getWidth() - PADDING, yOffset + PADDING, -1, TextRenderingOptions.SHADOW);
      yOffset += timerComponent.getHeight() + 1;

      // Render personal best
      context.canvas().submitRenderableComponent(personalBestComponent, WIDTH - personalBestComponent.getWidth() - PADDING, yOffset + PADDING, -1, TextRenderingOptions.SHADOW);
      context.canvas().submitRenderableComponent(personalBestLabelComponent, PADDING, yOffset + PADDING, -1, TextRenderingOptions.NONE);
    }

    size.set(WIDTH, yOffset + (PADDING * 2));
  }

  @Override
  public boolean isVisibleInGame() {
    return this.addon.playerParkourState().isRunSetUp();
  }

  private String formatTicks(long ticks) {
    if (!this.config.formatTicks().get()) {
      return String.valueOf(ticks);
    }
    return TickFormatter.formatTicks(ticks);
  }

  @Getter
  @Accessors(fluent = true)
  public static class RunSplitsWidgetConfig extends HudWidgetConfig {

    @SwitchSetting
    private final ConfigProperty<Boolean> formatTicks = new ConfigProperty<>(true);

    @ColorPickerSetting(alpha = true, chroma = true)
    private final ConfigProperty<Color> backgroundColor =
        new ConfigProperty<>(Color.ofRGB(0, 192, 255, 25));
  }

  private record SplitRow(
      RenderableComponent label,
      Optional<RenderableComponent> delta,
      RenderableComponent time
  ) { }
}
