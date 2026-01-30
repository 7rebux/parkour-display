package pw.rebux.parkourdisplay.core.util;

import net.labymod.api.Laby;
import net.labymod.api.client.render.batch.RectangleRenderContext;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;
import net.labymod.api.util.math.vector.IntVector3;

public final class RenderUtils {

  private static final RectangleRenderContext RECTANGLE_RENDER_CONTEXT =
      Laby.references().rectangleRenderContext();

  public static void renderAbsoluteBoundingBox(
      DoubleVector3 renderPosition,
      AxisAlignedBoundingBox boundingBox,
      float outlineThickness,
      Stack stack,
      Integer fillColor,
      Integer outlineColor
  ) {
    var minX = (float) (boundingBox.getMinX() - renderPosition.getX());
    var minY = (float) (boundingBox.getMinY() - renderPosition.getY());
    var minZ = (float) (boundingBox.getMinZ() - renderPosition.getZ());
    var maxX = (float) (boundingBox.getMaxX() - renderPosition.getX());
    var maxY = (float) (boundingBox.getMaxY() - renderPosition.getY());
    var maxZ = (float) (boundingBox.getMaxZ() - renderPosition.getZ());
    var width = maxX - minX;
    var height = maxY - minY;
    var depth = maxZ - minZ;

    // Bottom
    stack.push();
    stack.translate(minX, minY, minZ);
    stack.rotate(90f, 1f, 0f, 0f);
    drawOutlinedRectangle(stack, width, depth, outlineThickness, fillColor, outlineColor);
    stack.pop();

    // Top
    stack.push();
    stack.translate(minX, maxY, minZ);
    stack.rotate(90f, 1f, 0f, 0f);
    drawOutlinedRectangle(stack, width, depth, outlineThickness, fillColor, outlineColor);
    stack.pop();

    // Left
    stack.push();
    stack.translate(minX, minY, minZ);
    stack.rotate(-90f, 0f, 1f, 0f);
    drawOutlinedRectangle(stack, depth, height, outlineThickness, fillColor, outlineColor);
    stack.pop();

    // Right
    stack.push();
    stack.translate(maxX, minY, minZ);
    stack.rotate(-90f, 0f, 1f, 0f);
    drawOutlinedRectangle(stack, depth, height, outlineThickness, fillColor, outlineColor);
    stack.pop();

    // Front
    stack.push();
    stack.translate(minX, minY, minZ);
    drawOutlinedRectangle(stack, width, height, outlineThickness, fillColor, outlineColor);
    stack.pop();

    // Back
    stack.push();
    stack.translate(minX, minY, maxZ);
    drawOutlinedRectangle(stack, width, height, outlineThickness, fillColor, outlineColor);
    stack.pop();
  }

  public static void renderBoundingBox(
      IntVector3 objectPosition,
      DoubleVector3 renderPosition,
      AxisAlignedBoundingBox boundingBox,
      float outlineThickness,
      Stack stack,
      Integer fillColor,
      Integer outlineColor
  ) {
    renderBoundingBox(
        new DoubleVector3(objectPosition.getX(), objectPosition.getY(), objectPosition.getZ()),
        renderPosition,
        boundingBox,
        outlineThickness,
        stack,
        fillColor,
        outlineColor
    );
  }

  public static void renderBoundingBox(
      DoubleVector3 objectPosition,
      DoubleVector3 renderPosition,
      AxisAlignedBoundingBox boundingBox,
      float outlineThickness,
      Stack stack,
      Integer fillColor,
      Integer outlineColor
  ) {
    renderAbsoluteBoundingBox(
        renderPosition,
        boundingBox.move(objectPosition),
        outlineThickness,
        stack,
        fillColor,
        outlineColor
    );
  }

  private static void drawOutlinedRectangle(
      Stack stack,
      float width,
      float height,
      float outlineThickness,
      int fillColor,
      int outlineColor
  ) {
    RECTANGLE_RENDER_CONTEXT.begin(stack);
    RECTANGLE_RENDER_CONTEXT.render(0, 0, width, height, fillColor);
    RECTANGLE_RENDER_CONTEXT.renderOutline(
        0, 0, width, height, outlineThickness, outlineColor, outlineColor);
    RECTANGLE_RENDER_CONTEXT.uploadToBuffer(RenderPrograms.DEBUG_BOUNDING_BOX);
  }
}
