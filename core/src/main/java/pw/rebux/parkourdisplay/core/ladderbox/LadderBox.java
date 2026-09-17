package pw.rebux.parkourdisplay.core.ladderbox;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.core.offset.PersonalBest;

@Data
@RequiredArgsConstructor
public class LadderBox {

  private final AxisAlignedBoundingBox boundingBox;

  /// The climbable region the player's hitbox is tested against
  private final AxisAlignedBoundingBox intersectionBox;

  private final PersonalBest best = new PersonalBest();

  /// Offsets of the closest approach during the current airborne stretch, or null while the
  /// player is not attempting this box. The attempt's distance is derived from these, so that
  /// the two can never drift apart.
  ///
  /// Only sampled on ticks whose height could have caught the ladder, so this stays null for a
  /// flight that never reached the column's Y span, and its own Y component is always zero.
  @Nullable
  private DoubleVector3 attemptOffset;
}
