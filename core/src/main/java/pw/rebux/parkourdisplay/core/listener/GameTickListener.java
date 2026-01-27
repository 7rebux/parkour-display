package pw.rebux.parkourdisplay.core.listener;

import static net.labymod.api.client.component.Component.text;
import static net.labymod.api.client.component.Component.translatable;

import lombok.RequiredArgsConstructor;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.client.entity.player.GameMode;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Priority;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.state.PlayerParkourState;
import pw.rebux.parkourdisplay.core.state.TickPosition;

@RequiredArgsConstructor
public final class GameTickListener {

  private final ParkourDisplayAddon addon;

  private final TickPosition lastTick = new TickPosition();
  private int airTime = 0;
  private int groundTime = 0;

  // For last timing determination
  private int moveTime = -1;
  private int groundMovedTime = -1;
  private int jumpTime = -1;
  private int sneakTime = -2;
  private boolean locked = false;

  @Subscribe(Priority.FIRST)
  public void onGameTick(GameTickEvent event) {
    var minecraft = this.addon.labyAPI().minecraft();
    var player = minecraft.getClientPlayer();
    var playerParkourState = this.addon.playerParkourState();

    if (event.phase() == Phase.PRE
        || player == null
        || player.isAbilitiesFlying()
        || player.gameMode() == GameMode.SPECTATOR
        || minecraft.isPaused()
    ) {
      return;
    }

    updateLastTiming(playerParkourState);

    final var x = player.position().getX();
    final var y = player.position().getY();
    final var z = player.position().getZ();
    final var yaw = player.getRotationYaw();
    final var pitch = player.getRotationPitch();
    final var vx = x - lastTick.x();
    final var vz = z - lastTick.z();
    final var onGround = player.isOnGround();
    final var movingForward = player.getForwardMovingSpeed() != 0;
    final var movingSideways = tryGetMovingSideways(player);

    if (lastTick.onGround() && onGround) {
      groundTime = Math.min(groundTime + 1, Integer.MAX_VALUE);
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

    if (yaw != lastTick.yaw()) {
      playerParkourState.lastTurn(yaw - lastTick.yaw());
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
      playerParkourState.jumpYaw(yaw);
    }

    // Player landed in this tick
    if (onGround && !lastTick.onGround()) {
      groundMovedTime = 0;

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
      playerParkourState.hitYaw(yaw);
      playerParkourState.hitVelocityX(vx);
      playerParkourState.hitVelocityZ(vz);

      playerParkourState.groundDuration(0);
    }

    // Player attempted 45 degree strafe
    if (lastTick.movingForward() && !lastTick.movingSideways()
        && movingForward && movingSideways && !onGround) {
      playerParkourState.lastFF(yaw - lastTick.yaw());
    }

    playerParkourState.lastInput(buildInputString());

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

  private boolean tryGetMovingSideways(ClientPlayer player) {
    try {
      return player.getStrafeMovingSpeed() != 0;
    } catch (Throwable throwable) {
      return false;
    }
  }

  // https://www.mcpk.wiki/wiki/Timings
  private void updateLastTiming(PlayerParkourState playerParkourState) {
    var inputUtil = this.addon.minecraftInputUtil();

    // Movement
    if (inputUtil.isMoving()) {
      moveTime++;
      groundMovedTime++;

      if (jumpTime > -1 && moveTime == 0 && airTime != 0
          && (playerParkourState.lastTiming().contains("Pessi") || !locked)
      ) {
        if (jumpTime == 0) {
          playerParkourState.lastTiming("Max Pessi");
        } else {
          playerParkourState.lastTiming("Pessi %d ticks".formatted(jumpTime + 1));
        }
        locked = true;
      }
    } else {
      moveTime = -1;
      groundMovedTime = -1;
    }

    // Jumping
    if (inputUtil.jumpKey().isDown() && airTime == 0) { // Initiated jump
      jumpTime = 0;

      if (moveTime == 0) {
        playerParkourState.lastTiming("Jam");
        locked = true;
      } else if (moveTime > 0 && !locked) {
        if (sneakTime > -1) {
          playerParkourState.lastTiming("Burstjam %d ticks".formatted(groundMovedTime));
        } else if (sneakTime == -1) {
          playerParkourState.lastTiming("Burst %d ticks".formatted(groundMovedTime));
        } else {
          playerParkourState.lastTiming("HH %d ticks".formatted(groundMovedTime));
        }
        locked = true;
      }
    } else if (!lastTick.onGround() && jumpTime > -1) { // Midair
      jumpTime++;
    } else {
      jumpTime = -1;
    }

    // Sneaking
    if (inputUtil.sneakKey().isDown()) {
      sneakTime = sneakTime == -2 ? 0 : sneakTime + 1;
    }
    else {
      sneakTime = sneakTime < 0 ? -2 : -1;
    }

    // Unlock
    if (!inputUtil.isMoving() && airTime == 0) {
      locked = false;
    }
  }

  private String buildInputString() {
    var inputUtil = this.addon.minecraftInputUtil();
    var input = new StringBuilder();
    var hasMovement = false;

    if (inputUtil.forwardKey().isDown()) { input.append("W"); hasMovement = true; }
    if (inputUtil.leftKey().isDown())    { input.append("A"); hasMovement = true; }
    if (inputUtil.backKey().isDown())    { input.append("S"); hasMovement = true; }
    if (inputUtil.rightKey().isDown())   { input.append("D"); hasMovement = true; }

    if (inputUtil.jumpKey().isDown()) {
      if (hasMovement) input.append(" ");
      input.append("Jump");
    }

    if (inputUtil.sneakKey().isDown()) {
      if (hasMovement || !input.isEmpty()) input.append(" ");
      input.append("Sneak");
    }

    if (input.isEmpty()) {
      input.append("-");
    }

    return input.toString();
  }
}
