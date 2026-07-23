package pw.rebux.parkourdisplay.core.command.landingblock;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import org.spongepowered.include.com.google.common.primitives.Ints;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

public final class RemoveLandingBlockCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public RemoveLandingBlockCommand(ParkourDisplayAddon addon) {
    super("removelb", "rmlb", "clearlb");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var landingBlockRegistry = this.addon.landingBlockRegistry();

    if (arguments.length == 0) {
      landingBlockRegistry.landingBlocks().clear();
      ChatMessage.of(this, "successAll")
          .withColor(NamedTextColor.GREEN)
          .send();
    } else {
      var index = Ints.tryParse(arguments[0]);

      if (index == null || landingBlockRegistry.landingBlocks().size() <= index) {
        ChatMessage.of(this, "invalidIndex")
            .withColor(NamedTextColor.RED)
            .send();
        return true;
      }

      landingBlockRegistry.landingBlocks().remove(index.intValue());
      ChatMessage.of(this, "successSingle")
          .withColor(NamedTextColor.GREEN)
          .send();
    }

    return true;
  }
}
