package pw.rebux.parkourdisplay.common.run;

/// Determines how a split's bounding box counts as "hit" by the player's box.
public enum SplitBoxTriggerMode {
  Intersect,
  IntersectXZAboveY,
  IntersectXZSameY
}
