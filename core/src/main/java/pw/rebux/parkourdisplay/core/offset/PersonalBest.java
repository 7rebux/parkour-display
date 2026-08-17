package pw.rebux.parkourdisplay.core.offset;

import lombok.Data;
import org.jspecify.annotations.Nullable;

/// Tracks the largest distance submitted for a single target.
@Data
public final class PersonalBest {

  @Nullable
  private Double best;

  /// @return true if `distance` beats the stored best, which it then becomes.
  public boolean submit(double distance) {
    if (this.best != null && distance <= this.best) {
      return false;
    }

    this.best = distance;
    return true;
  }

  public void reset() {
    this.best = null;
  }
}
