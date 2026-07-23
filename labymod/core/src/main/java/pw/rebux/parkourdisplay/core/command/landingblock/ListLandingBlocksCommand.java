package pw.rebux.parkourdisplay.core.command.landingblock;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

public final class ListLandingBlocksCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public ListLandingBlocksCommand(ParkourDisplayAddon addon) {
    super("listlb", "lslb");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var landingBlocks = this.addon.landingBlockRegistry().landingBlocks();

    if (landingBlocks.isEmpty()) {
      ChatMessage.of(this, "empty")
          .withColor(NamedTextColor.RED)
          .send();
      return true;
    }

    for (int i = 0; i < landingBlocks.size(); i++) {
      var landingBlock = landingBlocks.get(i);

      ChatMessage.of(this, "entry")
          .withColor(NamedTextColor.GRAY)
          .withArgs(
              i,
              Component.text(landingBlock.label(), NamedTextColor.YELLOW),
              Component.text(landingBlock.mode().name(), NamedTextColor.GOLD)
          )
          .send();
    }

    return true;
  }
}
