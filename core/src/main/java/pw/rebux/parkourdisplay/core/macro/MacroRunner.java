package pw.rebux.parkourdisplay.core.macro;

import static net.labymod.api.client.component.Component.text;

import java.util.ArrayDeque;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@RequiredArgsConstructor
public final class MacroRunner {

  private final ParkourDisplayAddon addon;

  @Getter
  private final ArrayDeque<MacroTickState> activeMacro = new ArrayDeque<>();

  public void execute(List<MacroTickState> inputs) {
    if (!this.isAllowed()) {
      this.addon.displayMessageWithPrefix(
          text("Macros are disabled on this server.", NamedTextColor.RED));
      return;
    }

    this.addon.displayMessageWithPrefix(text("Running macro...", NamedTextColor.GREEN));
    this.activeMacro.clear();
    this.activeMacro.addAll(inputs);
  }

  private boolean isAllowed() {
    return this.addon.labyAPI()
        .permissionRegistry()
        .isPermissionEnabled(ParkourDisplayAddon.MACRO_PERMISSION);
  }
}
