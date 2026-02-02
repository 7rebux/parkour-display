package pw.rebux.parkourdisplay.core.command.run;

import static net.labymod.api.client.component.Component.translatable;

import java.util.Objects;
import java.util.Optional;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.run.split.RunSplit;
import pw.rebux.parkourdisplay.core.run.split.SplitBoxTriggerMode;
import pw.rebux.parkourdisplay.core.util.WorldUtils;

public final class AddRunSplitCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public AddRunSplitCommand(ParkourDisplayAddon addon) {
    super("addsplit", "setsplit", "as");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var player = Objects.requireNonNull(this.addon.labyAPI().minecraft().getClientPlayer());
    var runSplits = this.addon.runState().splits();

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

    runSplits.add(
        new RunSplit(
            "Split %d".formatted(runSplits.size() + 1),
            boundingBox,
            SplitBoxTriggerMode.IntersectXZSameY));

    this.displayMessage(
        translatable(
          "parkourdisplay.commands.addsplit.messages.success",
          NamedTextColor.GREEN));

    return true;
  }
}
