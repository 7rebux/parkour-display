package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.state.PlayerState;

/// The last performed jump timing.
///
/// References: [Timings - MCPK Wiki](https://www.mcpk.wiki/wiki/Timings)
public class LastTimingWidget extends TextHudWidget<TextHudWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;
  private String value = "-";

  private int moveTime = -1;
  private int groundMoveTime = -1;
  private int jumpTime = -1;
  private int sneakTime = -2;
  private int sprintTime = -1;
  private boolean lastForward;
  private boolean lastSideways;
  private boolean locked;

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

    if (state.isLandTick()) {
      this.groundMoveTime = 0;
    }

    this.textLine.updateAndFlush(value);
  }

  private void updateLastTiming(PlayerState state) {
    var inputUtil = this.addon.minecraftInputUtil();

    // Movement
    if (inputUtil.isMoving()) {
      moveTime++;
      groundMoveTime++;

      if (jumpTime > -1 && moveTime == 0 && state.airTime() != 0
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
      groundMoveTime = -1;
    }

    // Jumping
    if (inputUtil.jumpKey().isDown() && state.isJumpTick()) {
      jumpTime = 0;

      if (moveTime == 0) {
        this.value = "Jam";
        if (inputUtil.sprintKey().isDown() || !inputUtil.forwardKey().isDown()) {
          locked = true;
        }
      } else if (moveTime > 0 && !locked) {
        if (sneakTime > -1) {
          this.value = "Burstjam %dt".formatted(groundMoveTime);
        } else if (sneakTime == -1) {
          this.value = "Burst %dt".formatted(groundMoveTime);
        } else {
          this.value = "HH %dt".formatted(groundMoveTime);
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

    // FMM
    if ((inputUtil.sprintKey().isDown() || sprintTime != -1) && !state.lastTick().onGround() ) {
      sprintTime++;
      if (value.startsWith("Jam") && !locked && sprintTime == 0 && lastForward) {
        if (jumpTime >= 1) {
          if (jumpTime == 1) {
            value = "Max FMM";
          } else {
            value = "FMM " + (jumpTime) + " ticks";
          }

          locked = true;
        }
      }

    } else {
      sprintTime = -1;
    }

    // Mark
    if (state.airTime() > 0) {
      boolean pressingWNow = inputUtil.forwardKey().isDown();
      boolean wasPressingW = lastForward;

      if (pressingWNow && !wasPressingW && lastSideways) {
        this.value = "Mark %dt".formatted(state.airTime());
      }
    }

    // Unlock
    if (!inputUtil.isMoving() && state.currentTick().onGround()) {
      locked = false;
    }

    lastForward = inputUtil.forwardKey().isDown();
    lastSideways = inputUtil.isMovingSideways();
  }
}
