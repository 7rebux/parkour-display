package pw.rebux.parkourdisplay.core.util;

import net.labymod.api.util.math.MathHelper;

/// Interpolates a rotation between the tick it currently sits on and the following one, using a
/// uniform Catmull-Rom spline through the surrounding four tick rotations. Unlike a linear
/// interpolation the angular velocity does not jump at every tick, which is what makes a macro
/// look like it is turning by hand.
public final class RotationSpline {

  private boolean initialized = false;

  private float previous;
  private float from;
  private float to;
  private float following;

  /// Moves on to the segment between `from` and `to`. The rotation of the tick after
  /// `to` shapes the outgoing tangent, the rotation of the previous segment the incoming
  /// one. Both ends of a macro simply repeat their outermost rotation.
  public void advance(float from, float to, float following) {
    this.previous = this.initialized ? this.from : from;
    this.from = from;
    this.to = to;
    this.following = following;
    this.initialized = true;
  }

  public void reset() {
    this.initialized = false;
  }

  /// @return the rotation the current segment ends on, which is the exact rotation of the next tick
  public float target() {
    return this.to;
  }

  public float valueAt(float partialTicks) {
    // Relative to the segment start, so the seam at +-180 degrees is crossed the short way
    var incoming = MathHelper.wrapDegrees(this.previous - this.from);
    var end = MathHelper.wrapDegrees(this.to - this.from);
    var outgoing = end + MathHelper.wrapDegrees(this.following - this.to);

    var offset = 0.5F * partialTicks * (
        (end - incoming) + partialTicks * (
            (2.0F * incoming + 4.0F * end - outgoing) + partialTicks * (
                -incoming - 3.0F * end + outgoing)));

    return this.from + offset;
  }
}