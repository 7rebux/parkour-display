package pw.rebux.parkourdisplay.core.command.run;

import static net.labymod.api.client.component.Component.translatable;

import java.util.Objects;
import java.util.Optional;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.run.split.RunSplit;
import pw.rebux.parkourdisplay.core.util.WorldUtils;

public final class SetRunEndCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public SetRunEndCommand(ParkourDisplayAddon addon) {
    super("setend", "se");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var player = Objects.requireNonNull(this.addon.labyAPI().minecraft().getClientPlayer());

    // TODO: The boxes are not properly intersecting yet. Somehow pressure plates are only triggered
    //       if the player is at .125, but mine triggers already at .2 or something.
    //       Is it really the center of the players hitbox being checked?
    if (arguments.length > 0 && arguments[0].equals("pp")) {
      handlePressurePlateSplit();
      return true;
    }

    var blockStateOptional = WorldUtils.getBlockStandingOn();
    Optional<Double> customOffset = arguments.length > 0
        ? Optional.of(Double.parseDouble(arguments[0]))
        : Optional.empty();

    AxisAlignedBoundingBox boundingBox;

    if (customOffset.isPresent()) {
      boundingBox = new AxisAlignedBoundingBox(
          player.position().getX() - (customOffset.get() / 2),
          player.position().getY() - (customOffset.get() / 2),
          player.position().getZ() - (customOffset.get() / 2),
          player.position().getX() + (customOffset.get() / 2),
          player.position().getY() + (customOffset.get() / 2),
          player.position().getZ() + (customOffset.get() / 2)
      );
    } else if (blockStateOptional.isEmpty()) {
      boundingBox = new AxisAlignedBoundingBox(
          player.position().getX() - 1,
          player.position().getY() - 1,
          player.position().getZ() - 1,
          player.position().getX() + 1,
          player.position().getY() + 1,
          player.position().getZ() + 1
      );
    } else {
      var blockState = blockStateOptional.get();
      var absoluteBB = blockState.bounds().move(blockState.position());
      boundingBox = absoluteBB.minY(absoluteBB.getMaxY());
    }

    this.addon.runState().runEndSplit(new RunSplit("Finish", boundingBox));
    this.addon.displayMessage(
        translatable(
            "parkourdisplay.commands.setend.messages.success",
            NamedTextColor.GREEN));

    return true;
  }

  private void handlePressurePlateSplit() {
    var blockStateOptional = WorldUtils.getBlockStandingOn();

    if (blockStateOptional.isEmpty()) {
      this.displayMessage("Invalid block");
      return;
    }

    var blockState = blockStateOptional.get();
    var pp = this.addon.labyAPI().minecraft().clientWorld()
        .getBlockState(blockState.position().getX(), blockState.position().getY() + 1, blockState.position().getZ());

    if (pp == null || pp.bounds() == null || !pp.block().id().getPath().endsWith("pressure_plate")) {
      this.displayMessage("Block is not a pressure plate: " + pp.block().id().getPath());
      return;
    }

    this.addon.displayMessage(pp.bounds().toString());

    var boundingBox = pp.bounds().move(pp.position());
    this.addon.runState().runEndSplit(new RunSplit("Finish", boundingBox));
  }
}
