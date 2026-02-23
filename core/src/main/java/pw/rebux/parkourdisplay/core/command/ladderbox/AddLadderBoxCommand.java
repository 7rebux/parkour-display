package pw.rebux.parkourdisplay.core.command.ladderbox;

import net.labymod.api.client.chat.command.SubCommand;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;
import pw.rebux.parkourdisplay.core.util.WorldUtils;

public class AddLadderBoxCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public AddLadderBoxCommand(ParkourDisplayAddon addon) {
    super("addladderbox");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var targetBlock = WorldUtils.getBlockLookingAt();

    if (targetBlock.isEmpty() || !WorldUtils.isClimbable(targetBlock.get())) {
      ChatMessage.of(this, "invalidBlock").send();
      return true;
    }

    this.addon.ladderBoxRegistry().register(targetBlock.get());
    ChatMessage.of(this, "success").send();

    return true;
  }
}
