package pw.rebux.parkourdisplay.core.macro;

import java.util.ArrayDeque;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

@RequiredArgsConstructor
public final class MacroRunner {

  private final ParkourDisplayAddon addon;

  @Getter
  private final ArrayDeque<MacroTickState> activeMacro = new ArrayDeque<>();

  public void execute(List<MacroTickState> inputs) {
    if (!this.isAllowed()) {
      ChatMessage.of("messages.macros.disabled").send();
      return;
    }

    ChatMessage.of("messages.macros.running").send();
    this.activeMacro.clear();
    this.activeMacro.addAll(inputs);
  }

  private boolean isAllowed() {
    return this.addon.labyAPI()
        .permissionRegistry()
        .isPermissionEnabled(ParkourDisplayAddon.MACRO_PERMISSION);
  }
}
