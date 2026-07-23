package pw.rebux.parkourdisplay.common.macro;

import java.util.ArrayDeque;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pw.rebux.parkourdisplay.common.platform.Message;
import pw.rebux.parkourdisplay.common.platform.ParkourContext;
import pw.rebux.parkourdisplay.common.platform.TextColorType;

@RequiredArgsConstructor
public final class MacroRunner {

  private final ParkourContext context;

  @Getter
  private final ArrayDeque<MacroTickState> activeMacro = new ArrayDeque<>();

  public void execute(List<MacroTickState> inputs) {
    if (!this.context.isMacrosAllowed()) {
      this.context.chat().display(
          Message.of("messages.macros.disabled").color(TextColorType.RED));
      return;
    }

    this.context.chat().display(
        Message.of("messages.macros.running").color(TextColorType.GREEN));
    this.activeMacro.clear();
    this.activeMacro.addAll(inputs);
  }
}
