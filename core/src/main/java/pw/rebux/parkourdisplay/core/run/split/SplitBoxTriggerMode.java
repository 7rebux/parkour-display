package pw.rebux.parkourdisplay.core.run.split;

public enum SplitBoxTriggerMode {

  /// Typically used for pressure plate finishes.
  ///
  /// For example, HollowCube or 1.8 ModuleServer.
  Intersect,

  /// Checks if the players hitbox is intersecting on the XZ plane.
  /// And if the players minY is above the split box's maxY.
  ///
  /// For example, Cerkour, MatrixParkour.
  IntersectXZAboveY,

  /// Checks if the players hitbox is intersecting on the XZ plane,
  /// and additionally, if minY of the players hitbox is equal to maxX of the split box.
  ///
  /// Used for regular splits on top of blocks.
  IntersectXZSameY
}
