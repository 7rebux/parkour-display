package pw.rebux.parkourdisplay.core.listener;

import static net.labymod.api.client.component.Component.text;
import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.component.format.NamedTextColor;
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
  private int groundTime = 0;

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
    final var yaw = player.getRotationYaw();
    final var pitch = player.getRotationPitch();
    final var onGround = player.isOnGround();
    final var movingForward = player.getForwardMovingSpeed() != 0;
    final var movingSideways = false; // player.getStrafeMovingSpeed() != 0;

    playerParkourState.velocityX(x - lastTick.x());
    playerParkourState.velocityY(y - lastTick.y());
    playerParkourState.velocityZ(z - lastTick.z());

    if (lastTick.onGround() && onGround) {
      groundTime = Math.min(groundTime + 1, 999);
    }

    // If the player landed this tick or is still airborne, we increase the air time
    if (!lastTick.onGround() || !onGround) {
      airTime++;
    }

    if (groundTime > 0) {
      playerParkourState.groundDuration(groundTime);
    }

    if (airTime > 0) {
      playerParkourState.jumpDuration(airTime);
    }

    // Player jumped in this tick
    if (airTime == 1) {
      if (addon.configuration().showGroundDurations().get()) {
        var color = groundTime > 0 ? NamedTextColor.RED : NamedTextColor.GREEN;
        addon.displayMessage(
            text("%dt".formatted(groundTime), color)
                .append(text(" "))
                .append(translatable("parkourdisplay.labels.ground_time", NamedTextColor.GRAY)));
      }

      groundTime = 0;

      playerParkourState.jumpX(x);
      playerParkourState.jumpY(y);
      playerParkourState.jumpZ(z);
      playerParkourState.jumpYaw(yaw);
    }

    // Player landed in this tick
    if (onGround && !lastTick.onGround()) {
      if (addon.configuration().showJumpDurations().get()) {
        addon.displayMessage(
            text("%dt".formatted(airTime), NamedTextColor.GOLD)
                .append(text(" "))
                .append(translatable("parkourdisplay.labels.air_time", NamedTextColor.GRAY)));
      }

      playerParkourState.landingX(lastTick.x());
      playerParkourState.landingY(lastTick.y());
      playerParkourState.landingZ(lastTick.z());

      playerParkourState.hitX(x);
      playerParkourState.hitY(y);
      playerParkourState.hitZ(z);

      playerParkourState.groundDuration(0);
    }

    // Player attempted 45 degree strafe
    if (lastTick.movingForward() && !lastTick.movingSideways()
        && movingForward && movingSideways && !onGround) {
      playerParkourState.lastFF(yaw - lastTick.yaw());
    }

    /* EVERYTHING UNDER HERE WILL UPDATE VALUES FOR THE NEXT CALCULATIONS */

    if (onGround && airTime > 0) {
      airTime = 0;
    }

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
