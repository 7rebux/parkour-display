package pw.rebux.parkourdisplay.common.macro;

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
