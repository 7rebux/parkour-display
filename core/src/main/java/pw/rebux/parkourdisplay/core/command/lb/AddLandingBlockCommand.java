package pw.rebux.parkourdisplay.core.command.lb;

import static net.labymod.api.client.component.Component.translatable;

import java.util.Objects;
import java.util.Optional;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.world.block.BlockState;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class AddLandingBlockCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public AddLandingBlockCommand(ParkourDisplayAddon addon) {
    super("addlb", "setlb", "alb", "slb");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var blockState = this.getBlockStandingOn();

    if (blockState.isEmpty()) {
      this.displayMessage(
          translatable(
              "parkourdisplay.commands.addlb.messages.invalidBlock",
              NamedTextColor.RED));
      return true;
    }

    this.addon.landingBlockManager().register(blockState.get());
    this.displayMessage(
        translatable(
            "parkourdisplay.commands.addlb.messages.success",
            NamedTextColor.GREEN));

    return true;
  }

  /**
   * Finds block with collision at or below player
   */
  private Optional<BlockState> getBlockStandingOn() {
    var world = this.addon.labyAPI().minecraft().clientWorld();
    var player = Objects.requireNonNull(this.addon.labyAPI().minecraft().getClientPlayer());
    var position = player.position().toDoubleVector3();

    var inside = Optional.of(world.getBlockState(position))
        .filter(BlockState::hasCollision);
    var below = Optional.of(world.getBlockState(position.sub(0, 1 , 0)))
        .filter(BlockState::hasCollision);

    return inside.or(() -> below);
  }
}
