package pw.rebux.parkourdisplay.core.command.landingblock;

import net.labymod.api.client.chat.command.SubCommand;
import org.spongepowered.include.com.google.common.primitives.Ints;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

public final class ResetLandingBlockCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public ResetLandingBlockCommand(ParkourDisplayAddon addon) {
    super("resetlb");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var landingBlockRegistry = this.addon.landingBlockRegistry();

    if (arguments.length == 0) {
      landingBlockRegistry.landingBlocks().forEach(landingBlock ->
          landingBlock.bestDistance(null));
      ChatMessage.ofTranslatable(ChatMessage.commandKey(this, "successAll")).send();
    } else {
      var index = Ints.tryParse(arguments[0]);

      if (index == null || landingBlockRegistry.landingBlocks().size() <= index) {
        ChatMessage.ofTranslatable(ChatMessage.commandKey(this, "invalidIndex")).send();
        return true;
      }

      var landingBlock = landingBlockRegistry.landingBlocks().get(index);

      landingBlock.bestDistance(null);
      ChatMessage.ofTranslatable(ChatMessage.commandKey(this, "successSingle")).send();
    }

    return true;
  }
}
