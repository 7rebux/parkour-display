package pw.rebux.parkourdisplay.core.listener;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.entity.player.GameMode;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@RequiredArgsConstructor
public class GameTickListener {

  private final ParkourDisplayAddon addon;

  private boolean lastTickOnGround = false;
  private int airTime = 0;

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    if (event.phase() != Phase.POST) {
      return;
    }

    var player = addon.labyAPI().minecraft().getClientPlayer();

    if (player == null) {
      return;
    }

    if (player.isAbilitiesFlying() || player.gameMode() == GameMode.SPECTATOR) {
      return;
    }

    // If the player landed this tick or is still airborne, we increase the air time
    if (!lastTickOnGround || !player.isOnGround()) {
      airTime++;
    }

    if (airTime > 0) {
      addon.playerParkourState().airTime(airTime);
    }

    // Player jumped in this tick
    if (airTime == 1) {
      addon.playerParkourState().jumpX(player.position().getX());
      addon.playerParkourState().jumpY(player.position().getY());
      addon.playerParkourState().jumpZ(player.position().getZ());
      addon.playerParkourState().jumpYaw(player.getRotationYaw());
      addon.playerParkourState().jumpPitch(player.getRotationPitch());
    }

    // Player landed in this tick
    if (player.isOnGround() && !lastTickOnGround) {
      addon.playerParkourState().hitX(player.position().getX());
      addon.playerParkourState().hitY(player.position().getY());
      addon.playerParkourState().hitZ(player.position().getZ());
    }

    // Reset air time
    if (player.isOnGround()) {
      airTime = 0;
    }

    lastTickOnGround = player.isOnGround();
  }
}
