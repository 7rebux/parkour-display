package pw.rebux.parkourdisplay.core.command.run;

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
  private static final double plateMaxOffsetY = 0.25;
  private static final double plateInset = 1.0 / 16;

  private final ParkourDisplayAddon addon;

  public SetRunEndCommand(ParkourDisplayAddon addon) {
    super("setend", "se");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var targetBlockOptional = WorldUtils.getBlockLookingAt().or(WorldUtils::getBlockStandingOn);
    var mode = arguments.length > 0
        ? Arrays.stream(Mode.values())
          .filter(v -> v.toString().equalsIgnoreCase(arguments[0]))
          .findFirst()
          .orElseGet(() -> {
            this.displayMessage("Invalid mode. Using ground.");
            return Mode.Ground;
          })
        : Mode.Ground;

    if (targetBlockOptional.isEmpty()) {
      this.displayMessage("No block found.");
      return true;
    }

    var targetBlock = targetBlockOptional.get();

    if (!isPressurePlate(targetBlock) && (mode == Mode.Plate || mode == Mode.PlateOld)) {
      this.displayMessage("Target block is not a pressure plate.");
      return true;
    }

    var absoluteBB = targetBlock.bounds().move(targetBlock.position());
    SplitBoxTriggerMode triggerMode;

    switch (mode) {
      case Ground -> {
        absoluteBB = absoluteBB.minY(absoluteBB.getMaxY());
        triggerMode = SplitBoxTriggerMode.IntersectXZSameY;
      }
      // BB inset by 1/16 on all sides
      case Plate -> {
        absoluteBB = absoluteBB.maxY(absoluteBB.getMinY() + plateMaxOffsetY);
        triggerMode = SplitBoxTriggerMode.Intersect;
      }
      // Sensitive BB inset by 1/8 on all sides
      case PlateOld -> {
        absoluteBB = absoluteBB.maxY(absoluteBB.getMinY() + plateMaxOffsetY);
        // Regular BB is 1/16, so we add another 1/16 to end up on 1/8
        absoluteBB = absoluteBB.inflate(-plateInset, 0, -plateInset);
        triggerMode = SplitBoxTriggerMode.Intersect;
      }
      case BelowXYZ -> {
        absoluteBB = absoluteBB.inflate(-halfPlayerWidth, 0, -halfPlayerWidth);
        absoluteBB = absoluteBB.move(0, 1, 0);
        triggerMode = SplitBoxTriggerMode.Intersect;
      }
      case BelowXZ -> {
        absoluteBB = absoluteBB.inflate(-halfPlayerWidth, 0, -halfPlayerWidth);
        absoluteBB = absoluteBB.minY(absoluteBB.getMaxY());
        triggerMode = SplitBoxTriggerMode.IntersectXZAboveY;
      }
      default -> throw new IllegalStateException("Unexpected mode: " + mode);
    }

    var split = new RunSplit("Finish", absoluteBB, triggerMode);
    this.addon.runState().endSplit(split);

    this.displayTranslatable("success", NamedTextColor.GREEN);

    return true;
  }

  private boolean isPressurePlate(BlockState blockState) {
    return blockState.block().id().getPath().endsWith("pressure_plate");
  }

  private enum Mode {
    Ground,
    Plate,
    PlateOld,
    BelowXZ,
    BelowXYZ
  }
}
