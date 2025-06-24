package pw.rebux.parkourdisplay.core.command;

import static net.labymod.api.client.component.Component.translatable;

import java.util.Objects;
import java.util.Optional;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.world.block.BlockState;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class AddLandingBlockCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public AddLandingBlockCommand(ParkourDisplayAddon addon) {
    super("addlb", "setlb");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var blockState = this.getBlockStandingOn();

    if (blockState.isEmpty() || !blockState.get().hasCollision()) {
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

  private Optional<BlockState> getBlockStandingOn() {
    var player = Objects.requireNonNull(this.addon.labyAPI().minecraft().getClientPlayer());
    var world = this.addon.labyAPI().minecraft().clientWorld();

    var blockState = world.getBlockState(player.position().toDoubleVector3());

    if (blockState.hasCollision()) {
      return Optional.of(blockState);
    }

    blockState = world.getBlockState(player.position().toDoubleVector3().sub(0, 1, 0));

    return blockState.hasCollision() ? Optional.of(blockState) : Optional.empty();
  }
}
