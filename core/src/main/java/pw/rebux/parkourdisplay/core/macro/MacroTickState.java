package pw.rebux.parkourdisplay.core.macro;

public record MacroTickState(
    boolean w,
    boolean a,
    boolean s,
    boolean d,
    boolean jump,
    boolean sprint,
    boolean sneak,
    float yaw,
    float pitch
) {

}
