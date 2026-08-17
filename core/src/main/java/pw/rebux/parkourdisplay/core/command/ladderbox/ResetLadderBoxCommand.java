package pw.rebux.parkourdisplay.core.command.ladderbox;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import org.spongepowered.include.com.google.common.primitives.Ints;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

public final class ResetLadderBoxCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public ResetLadderBoxCommand(ParkourDisplayAddon addon) {
    super("resetladderbox");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var ladderBoxRegistry = this.addon.ladderBoxRegistry();

    if (arguments.length == 0) {
      ladderBoxRegistry.ladderBoxes().forEach(ladderBox -> ladderBox.best().reset());
      ChatMessage.of(this, "successAll")
          .withColor(NamedTextColor.GREEN)
          .send();
    } else {
      var index = Ints.tryParse(arguments[0]);

      if (index == null || ladderBoxRegistry.ladderBoxes().size() <= index) {
        ChatMessage.of(this, "invalidIndex")
            .withColor(NamedTextColor.RED)
            .send();
        return true;
      }

      ladderBoxRegistry.ladderBoxes().get(index).best().reset();
      ChatMessage.of(this, "successSingle")
          .withColor(NamedTextColor.GREEN)
          .send();
    }

    return true;
  }
}
