package pw.rebux.parkourdisplay.common.platform;

import pw.rebux.parkourdisplay.common.geom.Aabb;
import pw.rebux.parkourdisplay.common.geom.Vec3;

/// Read/write access to the local player for the current tick or frame. Implementations wrap the
/// platform's client player (LabyMod `ClientPlayer`, Fabric `ClientPlayerEntity`).
public interface PlayerAccess {

  Vec3 position();

  float yaw();

  float pitch();

  boolean onGround();

  /// @return an immutable copy of the player's bounding box (never a live reference — tick
  /// snapshots retain these).
  Aabb boundingBox();

  GameModeType gameMode();

  void setRotationYaw(float yaw);

  void setRotationPitch(float pitch);

  /// Sets the previous-frame rotation so the renderer does not interpolate a second time towards a
  /// rotation the macro engine already applied this tick.
  void setPreviousRotationYaw(float yaw);

  void setPreviousRotationPitch(float pitch);
}
