package pw.rebux.parkourdisplay.core.command.landingblock;

import static net.labymod.api.client.component.Component.space;
import static net.labymod.api.client.component.Component.text;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

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
      this.displayTranslatable("empty", NamedTextColor.RED);
      return true;
    }

    for (int i = 0; i < landingBlocks.size(); i++) {
      var landingBlock = landingBlocks.get(i);

      this.displayMessage(
          text()
              .append(text("#%d:".formatted(i), NamedTextColor.GRAY))
              .append(space())
              .append(landingBlock.label().color(NamedTextColor.YELLOW))
              .append(space())
              .append(text(landingBlock.mode().name(), NamedTextColor.GOLD))
              .build());
    }

    return true;
  }
}
