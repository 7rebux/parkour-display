package pw.rebux.parkourdisplay.core.command.run;

import static net.labymod.api.client.component.Component.translatable;

import java.util.Arrays;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.world.block.BlockState;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.run.split.RunSplit;
import pw.rebux.parkourdisplay.core.run.split.SplitBoxTriggerMode;
import pw.rebux.parkourdisplay.core.util.WorldUtils;

public final class SetRunEndCommand extends SubCommand {

  private static final double halfPlayerWidth = 0.3;
  // TODO: Shouldn't this be added in the intersection logic?
  private static final double epsilon = 1.0E-7;

  private final ParkourDisplayAddon addon;

  public SetRunEndCommand(ParkourDisplayAddon addon) {
    super("setend", "se");
    this.addon = addon;
  }

  // TODO: orElseThrow would be much cleaner, but what happens with it
  @Override
  public boolean execute(String prefix, String[] arguments) {
    var targetBlock = WorldUtils.getBlockLookingAt().or(WorldUtils::getBlockStandingOn)
        .orElseThrow(() -> new IllegalStateException("No block found"));
    var mode = arguments.length > 0
        ? Arrays.stream(Mode.values())
          .filter(v -> v.toString().toUpperCase().equalsIgnoreCase(arguments[0]))
          .findFirst()
          .orElse(null)
        : this.isPressurePlate(targetBlock) ? Mode.Plate : Mode.Default;

    if (mode == null) {
      this.displayMessage("Invalid mode");
      return true;
    }

    var absoluteBB = targetBlock.bounds().move(targetBlock.position());
    var triggerMode = SplitBoxTriggerMode.IntersectXZSameY;

    switch (mode) {
      case Default -> absoluteBB = absoluteBB.minY(absoluteBB.getMaxY());
      case Plate -> {
        absoluteBB = absoluteBB.maxY(absoluteBB.getMaxY() + epsilon);
        triggerMode = SplitBoxTriggerMode.Intersect;
      }
      case GroundXYZ -> {
        absoluteBB = absoluteBB.inflate(-halfPlayerWidth, 0, -halfPlayerWidth);
        absoluteBB = absoluteBB.move(0, 1, 0);
        triggerMode = SplitBoxTriggerMode.Intersect;
      }
      case GroundXZ -> {
        absoluteBB = absoluteBB.inflate(-halfPlayerWidth, 0, -halfPlayerWidth);
        absoluteBB = absoluteBB.minY(absoluteBB.getMaxY());
        triggerMode = SplitBoxTriggerMode.IntersectXZAboveY;
      }
    }

    var split = new RunSplit("Finish", absoluteBB, triggerMode);
    this.addon.runState().runEndSplit(split);

    this.addon.displayMessage(
        translatable(
            "parkourdisplay.commands.setend.messages.success",
            NamedTextColor.GREEN));

    return true;
  }

  private boolean isPressurePlate(BlockState blockState) {
    return blockState.block().id().getPath().endsWith("pressure_plate");
  }

  private enum Mode {
    Default,
    Plate,
    GroundXZ,
    GroundXYZ
  }
}
