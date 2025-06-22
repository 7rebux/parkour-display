package pw.rebux.parkourdisplay.core.command;

import java.util.Optional;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.Component;
import net.labymod.api.notification.Notification;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class SetLandingBlockCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public SetLandingBlockCommand(ParkourDisplayAddon addon) {
    super("setlb");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var minecraft = addon.labyAPI().minecraft();

    var blockState = Optional.ofNullable(minecraft.clientWorld().getBlockState(minecraft.getClientPlayer().position().toDoubleVector3().sub(0, 1, 0)));

    if (blockState.isEmpty()) {
      addon.displayMessage("You must be standing on a solid block to set the landing block.");
      return true;
    }

    if (!blockState.get().hasCollision()) {
      addon.displayMessage("You must be standing on a block with collision to set the landing block.");
      return true;
    }

    addon.landingBlockManager().register(blockState.get());

    addon.labyAPI().notificationController().push(
        Notification.builder()
            .title(Component.text("Landing Block"))
            .text(Component.text("Successfully added new landing block!"))
            .build());

    return true;

//    if (hitResult.type() != HitType.BLOCK) {
//      addon.displayMessage("You must be looking at a block to set the landing block.");
//      return false;
//    }
//
//    var targetBlock = minecraft.clientWorld().getBlockState(hitResult.location());
//
//    if (targetBlock.isFluid()) {
//      addon.displayMessage("You must be looking at a solid block to set the landing block.");
//      return false;
//    }
//
//    if (!targetBlock.hasCollision()) {
//      addon.displayMessage("You must be looking at a block with collision to set the landing block.");
//      return false;
//    }
//
//    addon.landingBlockManager().register(targetBlock);
//
//    addon.labyAPI().notificationController().push(
//        Notification.builder()
//            .title(Component.text("Landing Block"))
//            .text(Component.text("Successfully added new landing block!"))
//            .build());
//
//    return true;
  }
}
