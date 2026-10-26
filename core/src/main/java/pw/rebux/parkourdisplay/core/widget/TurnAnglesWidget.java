package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.text;

import lombok.Getter;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.TurnAnglesWidget.TurnAnglesWidgetConfig;

/// The yaw delta of every tick after jump initiation, one row per tick.
/// Rows are labeled by tick number, where tick 1 is the jump tick.
public final class TurnAnglesWidget extends TextHudWidget<TurnAnglesWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private String stringFormat;
  private int firstTick;
  private int lastTick;

  /// Ticks since jump initiation, or -1 if no jump is being tracked.
  private int tick = -1;

  public TurnAnglesWidget(ParkourDisplayAddon addon) {
    super("turn_angles", TurnAnglesWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(TurnAnglesWidgetConfig config) {
    super.load(config);

    var a = config.minTick().get();
    var b = config.maxTick().get();
    this.firstTick = Math.min(a, b);
    this.lastTick = Math.max(a, b);

    for (int i = this.firstTick; i <= this.lastTick; i++) {
      this.createLine(text(String.valueOf(i)), 0);
    }

    this.stringFormat = "%%.%df".formatted(config.decimalPlaces().get());
    this.tick = -1;
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var state = this.addon.playerState();

    if (state.isJumpTick()) {
      this.tick = 1;
      this.lines.forEach(line -> line.updateAndFlush(0));
    } else if (this.tick > 0) {
      this.tick++;
    } else {
      return;
    }

    if (this.tick >= this.firstTick) {
      var turn = String.format(this.stringFormat, state.yawTurn());
      this.lines.get(this.tick - this.firstTick).updateAndFlush(turn);
    }

    // Stop tracking once all rows are filled or the player landed
    if (this.tick >= this.lastTick || state.currentTick().onGround()) {
      this.tick = -1;
    }
  }

  @Getter
  public static class TurnAnglesWidgetConfig extends TextHudWidgetConfig {

    @SliderSetting(min = 1, max = 3)
    private final ConfigProperty<Integer> minTick = new ConfigProperty<>(2);

    @SliderSetting(min = 3, max = 32)
    private final ConfigProperty<Integer> maxTick = new ConfigProperty<>(12);

    @SliderSetting(min = 0, max = 10)
    private final ConfigProperty<Integer> decimalPlaces = new ConfigProperty<>(3);
  }
}
