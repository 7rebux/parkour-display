package pw.rebux.parkourdisplay.core.landingblock;

import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import org.jspecify.annotations.Nullable;

@Data
@RequiredArgsConstructor
public class LandingBlock {

  /// The label this [LandingBlock] is referred to in chat messages.
  private final String label;
  /// Determines which tick is used to compute the offsets.
  private final LandingBlockMode mode;
  /// Blocks might be made of multiple collision boxes.
  private final List<AxisAlignedBoundingBox> blockCollisions;

  @Nullable
  private Double bestDistance;
}
