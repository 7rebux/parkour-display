package pw.rebux.parkourdisplay.core.run;

import java.util.List;
import net.labymod.api.util.math.vector.DoubleVector3;
import pw.rebux.parkourdisplay.core.landingblock.LandingBlock;

public record RunExport(
    DoubleVector3 start,
    RunSplit end,
    List<RunSplit> splits,
    List<LandingBlock> landingBlocks
) {
}
