package pw.rebux.parkourdisplay.core.run;

import java.util.List;
import net.labymod.api.util.math.vector.DoubleVector3;
import pw.rebux.parkourdisplay.core.landingblock.LandingBlock;
import pw.rebux.parkourdisplay.core.run.split.Split;

public record RunExport(
    DoubleVector3 start,
    Split end,
    List<Split> splits,
    List<LandingBlock> landingBlocks
) {
}
