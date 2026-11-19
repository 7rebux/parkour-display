package pw.rebux.parkourdisplay.core.ladder;

import net.labymod.api.event.Event;

/// Fired on the landing tick of an airborne period that touched a ladder.
public record LadderClimbEvent(LadderClimb climb) implements Event {
}
