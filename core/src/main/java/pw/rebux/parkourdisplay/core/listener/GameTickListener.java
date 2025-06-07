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

  private TickPosition lastTick = new TickPosition();
  private TickPosition secondLastTick = new TickPosition();
  private int airtime = 0;

  public GameTickListener(ParkourDisplayAddon addon) {
    this.addon = addon;
    this.inputUtil = new MinecraftInputUtil(addon);
  }

  @Subscribe
  public void onGameTick(GameTickEvent event) {
    if (event.phase() != Phase.POST) {
      return;
    }

    var minecraft = addon.labyAPI().minecraft();
    var player = minecraft.getClientPlayer();
    var playerParkourState = addon.playerParkourState();

    if (player == null || player.isAbilitiesFlying() || player.gameMode() == GameMode.SPECTATOR || minecraft.isPaused()) {
      return;
    }

    final var x = player.position().getX();
    final var y = player.position().getY();
    final var z = player.position().getZ();
    final var velocityY = y - lastTick.y();
    final var yaw = player.getRotationYaw();
    final var pitch = player.getRotationPitch();
    final var onGround = player.isOnGround();
    final var movingForward = player.getForwardMovingSpeed() != 0;
    final var movingSideways = false; // player.getStrafeMovingSpeed() != 0;

    playerParkourState.velocityX(x - lastTick.x());
    playerParkourState.velocityY(velocityY);
    playerParkourState.velocityZ(z - lastTick.z());

    // If the player landed this tick or is still airborne, we increase the air time
    if (!lastTick.onGround() || !onGround) {
      airtime++;
    }

    if (airtime > 0) {
      playerParkourState.lastDuration(airtime);
    }

    // Player jumped in this tick
    if (airtime == 1) {
      playerParkourState.jumpX(x);
      playerParkourState.jumpY(y);
      playerParkourState.jumpZ(z);
      playerParkourState.jumpYaw(yaw);
      playerParkourState.jumpPitch(pitch);
    }

    // Player landed in this tick
    if (onGround && !lastTick.onGround()) {
      playerParkourState.landingX(lastTick.x());
      playerParkourState.landingY(lastTick.y());
      playerParkourState.landingZ(lastTick.z());
      playerParkourState.hitX(x);
      playerParkourState.hitY(y);
      playerParkourState.hitZ(z);
    }

    // Player attempted 45 degree strafe
    if (lastTick.movingForward() && !lastTick.movingSideways()
        && movingForward && movingSideways && !onGround) {
      playerParkourState.lastFF(yaw - lastTick.yaw());
    }

    // Player is falling
    if (velocityY < 0 && !onGround) {
      addon.landingBlockManager().checkOffsets(player.position(), lastTick, secondLastTick);
    }

    /* EVERYTHING UNDER HERE WILL UPDATE VALUES FOR THE NEXT CALCULATIONS */

    if (onGround && airtime > 0) {
      airtime = 0;
    }

    // TODO: Is this a reference?
    secondLastTick = lastTick;
    lastTick.x(x);
    lastTick.y(y);
    lastTick.z(z);
    lastTick.yaw(yaw);
    lastTick.pitch(pitch);
    lastTick.onGround(onGround);
    lastTick.movingForward(movingForward);
    lastTick.movingSideways(movingSideways);
  }
}
