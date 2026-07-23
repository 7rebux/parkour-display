package pw.rebux.parkourdisplay.core.run;

import java.util.List;
import pw.rebux.parkourdisplay.common.geom.Vec3;
import pw.rebux.parkourdisplay.common.run.RunSplit;
import pw.rebux.parkourdisplay.core.landingblock.LandingBlock;

public record RunExport(
    Vec3 start,
    RunSplit end,
    List<RunSplit> splits,
    List<LandingBlock> landingBlocks
) {
}
