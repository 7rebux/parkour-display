package pw.rebux.parkourdisplay.core.listener;

import net.labymod.api.client.entity.player.GameMode;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.state.TickPosition;
import pw.rebux.parkourdisplay.core.util.MinecraftInputUtil;

public class GameTickListener {

  private final ParkourDisplayAddon addon;
  private final MinecraftInputUtil inputUtil;

  private final TickPosition lastTick = new TickPosition();
  private int airTime = 0;

  public GameTickListener(ParkourDisplayAddon addon) {
    this.addon = addon;
    this.inputUtil = new MinecraftInputUtil(addon);
  }

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
    if (!lastTick.onGround() || !player.isOnGround()) {
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
    if (player.isOnGround() && !lastTick.onGround()) {
      addon.playerParkourState().landingX(lastTick.x());
      addon.playerParkourState().landingY(lastTick.y());
      addon.playerParkourState().landingZ(lastTick.z());

      addon.playerParkourState().hitX(player.position().getX());
      addon.playerParkourState().hitY(player.position().getY());
      addon.playerParkourState().hitZ(player.position().getZ());
    }

    // Reset air time
    if (player.isOnGround()) {
      airTime = 0;
    }

    lastTick.x(player.position().getX());
    lastTick.y(player.position().getY());
    lastTick.z(player.position().getZ());
    lastTick.onGround(player.isOnGround());
  }
}
