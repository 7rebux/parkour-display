package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

/// The sidestep direction swap performed on or after the jump tick.
///
/// WDWA → Instant direction swap on jump tick
/// WAD → Delayed sideways input after jump
public final class LastSidestepWidget extends TextHudWidget<TextHudWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private TextLine textLine;

  private int sidestep = -1;
  private boolean wadStart = false;
  private boolean lastLeftDown = false;
  private boolean lastRightDown = false;
  private boolean lastMovingSideways = false;
  private long lastAirTime = 0;

  public LastSidestepWidget(ParkourDisplayAddon addon) {
    super("last_sidestep");
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(TextHudWidgetConfig config) {
    super.load(config);
    this.textLine = createLine(translatable("parkourdisplay.labels.last_sidestep"), "-");
  }

  @Override
  public void onTick(boolean isEditorContext) {
    this.updateLastSidestep();

    var formatted = switch (this.sidestep) {
      case -1 -> "-";
      case 0 -> "WDWA";
      default -> "WAD %dt".formatted(this.sidestep);
    };

    this.textLine.updateAndFlush(formatted);
  }

  private void updateLastSidestep() {
    var state = this.addon.playerState();
    var inputUtil = this.addon.minecraftInputUtil();
    var leftDown = inputUtil.leftKey().isDown();
    var rightDown = inputUtil.rightKey().isDown();
    var movingSideways = inputUtil.isMovingSideways();

    if (state.isJumpTick()) {
      this.wadStart = !movingSideways;

      if (movingSideways) {
        var swappedDirection = this.lastLeftDown && rightDown || this.lastRightDown && leftDown;
        this.sidestep = swappedDirection ? 0 : -1;
      } else if (!this.lastMovingSideways) {
        this.sidestep = -1;
      }
    } else if (this.wadStart && movingSideways && this.lastAirTime != state.airTime()) {
      this.sidestep = (int) this.lastAirTime;
      this.wadStart = false;
    }

    this.lastLeftDown = leftDown;
    this.lastRightDown = rightDown;
    this.lastMovingSideways = movingSideways;
    this.lastAirTime = state.airTime();
  }
}
