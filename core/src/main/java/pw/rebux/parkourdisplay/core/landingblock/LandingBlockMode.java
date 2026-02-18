package pw.rebux.parkourdisplay.core.landingblock;

/// [Collisions - MCPK Wiki](https://www.mcpk.wiki/wiki/Collisions)
public enum LandingBlockMode {

  /// Will compare the bounding box with the players' hitbox at the landing tick.
  /// This will be what you want for any jump where you are trying to land on a block.
  Land,

  /// Will use the player hitbox of the hit tick, which is one tick after the landing tick.
  /// You will want this, for example, when trying to bounce on slime.
  Hit,
}
