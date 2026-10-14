package pw.rebux.parkourdisplay.core.util;

import net.labymod.api.laby3d.pipeline.RenderStates;
import net.labymod.api.laby3d.vertex.VertexDescriptions;
import net.labymod.api.util.logging.Logging;
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

  // TODO(debug): Temporary investigation of boxes not rendering on 26.2+. Under REVERSED_Z,
  //  laby3d's DepthConvention.translate() negates ALWAYS into NEVER, which the RHI backend
  //  applies literally. Passing NEVER makes it come out as ALWAYS after translation.
  //  Run with -Dparkourdisplay.reversedZFix=false to reproduce the bug.
  private static final boolean REVERSED_Z_FIX =
      Boolean.parseBoolean(System.getProperty("parkourdisplay.reversedZFix", "true"));
  private static final ComparisonStrategy ALWAYS_DEPTH_STRATEGY = alwaysDepthStrategy();

  public static final RenderState DEBUG_BOUNDING_BOX = RenderState.builder()
      .setId(buildStateId("debug_bounding_box"))
      .setVertexDescription(VertexDescriptions.POSITION_UV_COLOR)
      .setDrawingMode(DrawingMode.QUADS)
      .setBlendFunction(DefaultBlendFunctions.TRANSLUCENT)
      .setCull(false)
      .setDepthTestStrategy(ALWAYS_DEPTH_STRATEGY)
      .setWriteDepth(false)
      .setShaderProgramDescription(
          ShaderProgramDescription.builder(RenderStates.DEFAULT_SHADER_SNIPPET)
              .setId(buildProgramId("quads"))
              .setVertexShader(RenderStates.SHADER_RESOLVER.apply("core/position_color.vsh"))
              .setFragmentShader(RenderStates.SHADER_RESOLVER.apply("core/position_color.fsh"))
              .build()
      )
      .build();

  private static ComparisonStrategy alwaysDepthStrategy() {
    var convention = DepthConventionHolder.get();
    var strategy = REVERSED_Z_FIX && convention == DepthConvention.REVERSED_Z
        ? ComparisonStrategy.NEVER
        : ComparisonStrategy.ALWAYS;
    Logging.create(RenderPrograms.class).info(
        "[DepthDebug] convention={}, fix={}, strategy={}, translated={}",
        convention, REVERSED_Z_FIX, strategy, convention.translate(strategy));
    return strategy;
  }

  private static AssetId buildStateId(String name) {
    return AssetId.of(ParkourDisplayAddon.NAMESPACE, "renderstate/" + name);
  }

  private static AssetId buildProgramId(String name) {
    return AssetId.of(ParkourDisplayAddon.NAMESPACE, "shaderprogram/" + name);
  }
}
