package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.state.PlayerState;

public class LastTimingWidget extends TextHudWidget<TextHudWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;
  private String value = "-";

  // TODO: Duplicated kinda
  private int airTime = 0;

  private int moveTime = -1;
  private int groundMovedTime = -1;
  private int jumpTime = -1;
  private int sneakTime = -2;
  private boolean locked = false;

  public LastTimingWidget(ParkourDisplayAddon addon) {
    super("last_timing");
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(TextHudWidgetConfig config) {
    super.load(config);

    this.textLine = createLine(
        translatable("parkourdisplay.labels.last_timing"),
        this.value
    );
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var state = this.addon.playerState();

    this.updateLastTiming(state);

    // If the player landed this tick or is still airborne, we increase the air time
    if (!state.lastTick().onGround() || state.currentTick().onGround()) {
      this.airTime++;
    }

    // Player landed in this tick
    if (state.currentTick().onGround() && !state.lastTick().onGround()) {
      this.groundMovedTime = 0;
    }

    this.textLine.updateAndFlush(value);

    // TODO: This should always happen in the LAST event
    if (state.currentTick().onGround()) {
      this.airTime = 0;
    }
  }

  // https://www.mcpk.wiki/wiki/Timings
  private void updateLastTiming(PlayerState state) {
    var inputUtil = this.addon.minecraftInputUtil();

    // Movement
    if (inputUtil.isMoving()) {
      moveTime++;
      groundMovedTime++;

      if (jumpTime > -1 && moveTime == 0 && airTime != 0
          && (this.value.contains("Pessi") || !locked)
      ) {
        if (jumpTime == 0) {
          this.value = "Max Pessi";
        } else {
          this.value = "Pessi %dt".formatted(jumpTime + 1);
        }
        locked = true;
      }
    } else {
      moveTime = -1;
      groundMovedTime = -1;
    }

    // Jumping
    if (inputUtil.jumpKey().isDown() && airTime == 0) { // Initiated jump
      jumpTime = 0;

      if (moveTime == 0) {
        this.value = "Jam";
        locked = true;
      } else if (moveTime > 0 && !locked) {
        if (sneakTime > -1) {
          this.value = "Burstjam %dt".formatted(groundMovedTime);
        } else if (sneakTime == -1) {
          this.value = "Burst %dt".formatted(groundMovedTime);
        } else {
          this.value = "HH %dt".formatted(groundMovedTime);
        }
        locked = true;
      }
    } else if (!state.lastTick().onGround() && jumpTime > -1) { // Midair
      jumpTime++;
    } else {
      jumpTime = -1;
    }

    // Sneaking
    if (inputUtil.sneakKey().isDown()) {
      sneakTime = sneakTime == -2 ? 0 : sneakTime + 1;
    }
    else {
      sneakTime = sneakTime < 0 ? -2 : -1;
    }

    // Unlock
    if (!inputUtil.isMoving() && airTime == 0) {
      locked = false;
    }
  }
}
