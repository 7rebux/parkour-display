package pw.rebux.parkourdisplay.core.util;

import net.labymod.api.Laby;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.util.bounds.Rectangle;
import net.labymod.api.util.math.AxisAlignedBoundingBox;
import net.labymod.api.util.math.vector.DoubleVector3;
import net.labymod.api.util.math.vector.IntVector3;

public final class RenderUtil {

  private static final RectangleRenderer rectangleRenderer = Laby.references().renderPipeline().rectangleRenderer();

  // TODO: Faces not rendering in v1.8.9
  public static void renderBoundingBox(
      IntVector3 objectPosition,
      DoubleVector3 renderPosition,
      AxisAlignedBoundingBox boundingBox,
      float outlineThickness,
      Stack stack,
      Integer fillColor,
      Integer outlineColor
  ) {
    var absBB = boundingBox.move(objectPosition);
    var minX = (float) (absBB.getMinX() - renderPosition.getX());
    var minY = (float) (absBB.getMinY() - renderPosition.getY());
    var minZ = (float) (absBB.getMinZ() - renderPosition.getZ());
    var maxX = (float) (absBB.getMaxX() - renderPosition.getX());
    var maxY = (float) (absBB.getMaxY() - renderPosition.getY());
    var maxZ = (float) (absBB.getMaxZ() - renderPosition.getZ());
    var width = maxX - minX;
    var height = maxY - minY;
    var depth = maxZ - minZ;

    // Bottom
    stack.push();
    stack.translate(minX, minY, minZ);
    stack.rotate(90f, 1f, 0f, 0f);
    rectangleRenderer.pos(0, 0, width, depth).color(fillColor).render(stack);
    rectangleRenderer.renderOutline(stack, Rectangle.absolute(0, 0, width - outlineThickness, depth - outlineThickness), outlineColor, outlineThickness);
    stack.pop();

    // Top
    stack.push();
    stack.translate(minX, maxY, minZ);
    stack.rotate(90f, 1f, 0f, 0f);
    rectangleRenderer.pos(0, 0, width, depth).color(fillColor).render(stack);
    rectangleRenderer.renderOutline(stack, Rectangle.absolute(0, 0, width - outlineThickness, depth - outlineThickness), outlineColor, outlineThickness);
    stack.pop();

    // Left
    stack.push();
    stack.translate(minX, minY, minZ);
    stack.rotate(-90f, 0f, 1f, 0f);
    rectangleRenderer.pos(0, 0, depth, height).color(fillColor).render(stack);
    rectangleRenderer.renderOutline(stack, Rectangle.absolute(0, 0, depth - outlineThickness, height - outlineThickness), outlineColor, outlineThickness);
    stack.pop();

    // Right
    stack.push();
    stack.translate(maxX, minY, minZ);
    stack.rotate(-90f, 0f, 1f, 0f);
    rectangleRenderer.pos(0, 0, depth, height).color(fillColor).render(stack);
    rectangleRenderer.renderOutline(stack, Rectangle.absolute(0, 0, depth - outlineThickness, height - outlineThickness), outlineColor, outlineThickness);
    stack.pop();

    // Front
    stack.push();
    stack.translate(minX, minY, minZ);
    rectangleRenderer.pos(0, 0, width, height).color(fillColor).render(stack);
    rectangleRenderer.renderOutline(stack, Rectangle.absolute(0, 0, width - outlineThickness, height - outlineThickness), outlineColor, outlineThickness);
    stack.pop();

    // Back
    stack.push();
    stack.translate(minX, minY, maxZ);
    rectangleRenderer.pos(0, 0, width, height).color(fillColor).render(stack);
    rectangleRenderer.renderOutline(stack, Rectangle.absolute(0, 0, width - outlineThickness, height - outlineThickness), outlineColor, outlineThickness);
    stack.pop();
  }
}
