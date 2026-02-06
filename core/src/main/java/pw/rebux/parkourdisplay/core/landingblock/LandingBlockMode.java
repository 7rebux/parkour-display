package pw.rebux.parkourdisplay.core.landingblock;

/// [Collisions - MCPK Wiki](https://www.mcpk.wiki/wiki/Collisions)
public enum LandingBlockMode {

  /// Will compare the bounding box with the players' hitbox at the landing tick.
  /// This will be what you want for any jump where you are trying to land on a block.
  Land,

  /// Will use the player hitbox of the hit tick, which is one tick after the landing tick.
  /// You will want this, for example, when trying to bounce on slime.
  Hit,

  /// Will use the hitbox of the tick before the landing tick.
  /// You will want this when trying to land a Z-facing Neo (In old versions).
  /// Or when trying to pass a blockage in the direction with less velocity for 1.14+.
  ZNeo;
}
