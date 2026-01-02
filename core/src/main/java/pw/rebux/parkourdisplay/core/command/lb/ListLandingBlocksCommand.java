package pw.rebux.parkourdisplay.core.command.lb;

import static net.labymod.api.client.component.Component.space;
import static net.labymod.api.client.component.Component.text;
import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.TextComponent;
import net.labymod.api.client.component.TranslatableComponent;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.world.block.Block;
import net.labymod.api.util.math.vector.IntVector3;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class ListLandingBlocksCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public ListLandingBlocksCommand(ParkourDisplayAddon addon) {
    super("listlb", "lslb");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var landingBlocks = this.addon.landingBlockManager().getLandingBlocks();

    if (landingBlocks.isEmpty()) {
      this.displayMessage(
          translatable("parkourdisplay.commands.listlb.messages.empty", NamedTextColor.RED));
      return true;
    }

    for (int i = 0; i < landingBlocks.size(); i++) {
      var landingBlock = landingBlocks.get(i);

      this.displayMessage(
          text()
              .append(text("#%d:".formatted(i), NamedTextColor.GRAY))
              .append(space())
              .append(blockDisplayName(landingBlock.block()).color(NamedTextColor.YELLOW))
              .append(space())
              .append(fromIntVector3(landingBlock.blockPosition()).color(NamedTextColor.GOLD))
              .build());
    }

    return true;
  }

  private TextComponent fromIntVector3(IntVector3 position) {
    return text(position.getX())
        .append(text(", "))
        .append(text(position.getY()))
        .append(text(", "))
        .append(text(position.getZ()));
  }

  private TranslatableComponent blockDisplayName(Block block) {
    return translatable("block.minecraft.%s".formatted(block.id().getPath()));
  }
}
