package pw.rebux.parkourdisplay.api;

public enum SplitBoxTriggerMode {

  /// Typically used for pressure plate finishes.
  Intersect,

  /// Checks if the players hitbox is intersecting on the XZ plane.
  /// And if the players minY is above the split box's maxY.
  IntersectXZAboveY,

  /// Checks if the players hitbox is intersecting on the XZ plane,
  /// and additionally, if minY of the players hitbox is equal to maxY of the split box.
  IntersectXZSameY
}
