package pw.rebux.parkourdisplay.core.command;

import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.world.phys.hit.HitResult.HitType;
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
    var hitResult = minecraft.getHitResult();

    if (hitResult.type() != HitType.BLOCK) {
      addon.sendMessage("You must be looking at a block to set the landing block.");
      return false;
    }

    var targetBlock = minecraft.clientWorld().getBlockState(hitResult.location());

    if (targetBlock.isFluid()) {
      addon.sendMessage("You must be looking at a solid block to set the landing block.");
      return false;
    }

    if (!targetBlock.hasCollision()) {
      addon.sendMessage("You must be looking at a block with collision to set the landing block.");
    }

    addon.landingBlockManager().register(targetBlock);

    addon.labyAPI().notificationController().push(
        Notification.builder()
            .title(Component.text("Landing Block"))
            .text(Component.text("Successfully added new landing block!"))
            .build());

    return true;
  }
}
