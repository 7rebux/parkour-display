package pw.rebux.parkourdisplay.core.graph;

/// A single tick within a recorded jump.
///
/// @param tick        The jump-local tick index (0 = the ground the jump started from, then the
///                    air time on each airborne tick).
/// @param positionY   The absolute Y position that tick. The graph normalizes this against the
///                    first sample so the jump starts at 0.
/// @param onClimbable Whether the player was on a climbable (ladder/vine) that tick.
public record JumpSample(int tick, double positionY, boolean onClimbable) {
}
