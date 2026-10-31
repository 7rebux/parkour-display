package pw.rebux.parkourdisplay.core.landingblock;

import lombok.Data;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.core.util.WorldUtils;

@Data
public class LandingBlock {

  /// The label this [LandingBlock] is referred to in chat messages.
  private final String label;
  /// Determines which tick is used to compute the offsets.
  private LandingBlockMode mode;
  /// The unmodified box of the block, used to recompute [#collisionBox] when the mode changes.
  /// Missing for landing blocks loaded from run files saved before this field existed.
  @Nullable
  private final AxisAlignedBoundingBox blockBox;
  private final boolean climbable;
  private AxisAlignedBoundingBox collisionBox;

  @Nullable
  private Double bestDistance;

  public LandingBlock(
      String label,
      LandingBlockMode mode,
      AxisAlignedBoundingBox blockBox,
      boolean climbable
  ) {
    this.label = label;
    this.blockBox = blockBox;
    this.climbable = climbable;
    this.mode(mode);
  }

  public void mode(LandingBlockMode mode) {
    this.mode = mode;

    if (this.blockBox != null) {
      this.collisionBox = mode == LandingBlockMode.Enter && this.climbable
          ? WorldUtils.ladderIntersectionBox(this.blockBox)
          : this.blockBox;
    }
  }
}
