package pw.rebux.parkourdisplay.core.ladder;

/// Tricks a LadderClimb replay can add on top of the recorded climb inputs.
public enum MissedTrick {
  /// Hold the height instead of falling on the ladder before the first climb tick.
  SNEAK,
  /// Release the second to last climb tick, climb once more, then release for good.
  RELEASE,
  /// Release the last climb tick as soon as the release velocity still clears the top.
  EARLY_EXIT
}
