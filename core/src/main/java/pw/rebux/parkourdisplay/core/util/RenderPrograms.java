package pw.rebux.parkourdisplay.core.util;

import net.labymod.api.laby3d.pipeline.RenderStates;
import net.labymod.api.laby3d.vertex.VertexDescriptions;
import net.labymod.laby3d.api.pipeline.ComparisonStrategy;
import net.labymod.laby3d.api.pipeline.DepthConvention;
import net.labymod.laby3d.api.pipeline.DepthConventionHolder;
import net.labymod.laby3d.api.pipeline.DrawingMode;
import net.labymod.laby3d.api.pipeline.RenderState;
import net.labymod.laby3d.api.pipeline.blend.DefaultBlendFunctions;
import net.labymod.laby3d.api.pipeline.shader.ShaderProgramDescription;
import net.labymod.laby3d.api.resource.AssetId;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public final class RenderPrograms {

  public static final RenderState DEBUG_BOUNDING_BOX = RenderState.builder()
      .setId(buildStateId("debug_bounding_box"))
      .setVertexDescription(VertexDescriptions.POSITION_UV_COLOR)
      .setDrawingMode(DrawingMode.QUADS)
      .setBlendFunction(DefaultBlendFunctions.TRANSLUCENT)
      .setCull(false)
      .setDepthTestStrategy(alwaysDepthStrategy())
      .setWriteDepth(false)
      .setShaderProgramDescription(
          ShaderProgramDescription.builder(RenderStates.DEFAULT_SHADER_SNIPPET)
              .setId(buildProgramId("quads"))
              .setVertexShader(RenderStates.SHADER_RESOLVER.apply("core/position_color.vsh"))
              .setFragmentShader(RenderStates.SHADER_RESOLVER.apply("core/position_color.fsh"))
              .build()
      )
      .build();

  // Under REVERSED_Z, laby3d's DepthConvention.translate() negates ALWAYS into NEVER, which the
  // RHI backend (26.2+) applies literally, so nothing gets drawn. Passing NEVER makes it come out
  // as ALWAYS after translation. Remove once laby3d only swaps the LESS/GREATER pairs.
  private static ComparisonStrategy alwaysDepthStrategy() {
    return DepthConventionHolder.get() == DepthConvention.REVERSED_Z
        ? ComparisonStrategy.NEVER
        : ComparisonStrategy.ALWAYS;
  }

  private static AssetId buildStateId(String name) {
    return AssetId.of(ParkourDisplayAddon.NAMESPACE, "renderstate/" + name);
  }

  private static AssetId buildProgramId(String name) {
    return AssetId.of(ParkourDisplayAddon.NAMESPACE, "shaderprogram/" + name);
  }
}
