package pw.rebux.parkourdisplay.core.util;

import net.labymod.api.laby3d.pipeline.RenderStates;
import net.labymod.api.laby3d.vertex.VertexDescriptions;
import net.labymod.laby3d.api.pipeline.ComparisonStrategy;
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
      .setDepthTestStrategy(ComparisonStrategy.ALWAYS)
      .setWriteDepth(false)
      .setShaderProgramDescription(
          ShaderProgramDescription.builder(RenderStates.DEFAULT_SHADER_SNIPPET)
              .setId(buildProgramId("quads"))
              .setVertexShader(RenderStates.SHADER_RESOLVER.apply("core/position_color.vsh"))
              .setFragmentShader(RenderStates.SHADER_RESOLVER.apply("core/position_color.fsh"))
              .build()
      )
      .build();

  private static AssetId buildStateId(String name) {
    return AssetId.of(ParkourDisplayAddon.NAMESPACE, "renderstate/" + name);
  }

  private static AssetId buildProgramId(String name) {
    return AssetId.of(ParkourDisplayAddon.NAMESPACE, "shaderprogram/" + name);
  }
}
