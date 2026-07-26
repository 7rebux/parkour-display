package pw.rebux.parkourdisplay.core.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.List;

/// Renders a jump's height curve (x = tick, y = Y position normalized so the jump starts at 0)
/// into a {@link BufferedImage}. Pure and free of Minecraft types so it can be tested in isolation.
public final class JumpGraphRenderer {

  static {
    // Ensure AWT never tries to open a display (avoids macOS/headless quirks).
    System.setProperty("java.awt.headless", "true");
  }

  private static final int WIDTH = 960;
  private static final int HEIGHT = 540;
  private static final int MARGIN_LEFT = 76;
  private static final int MARGIN_RIGHT = 28;
  private static final int MARGIN_TOP = 60;
  private static final int MARGIN_BOTTOM = 48;

  private static final Color BACKGROUND = new Color(0x1E1F22);
  private static final Color PLOT_BACKGROUND = new Color(0x2B2D31);
  private static final Color GRID = new Color(0x3A3D42);
  private static final Color AXIS = new Color(0x8A8F98);
  private static final Color TEXT = new Color(0xDCDDDE);
  private static final Color ZERO_LINE = new Color(0xB9BBBE);
  private static final Color CURVE = new Color(0x9AA0A6);
  private static final Color UP = new Color(0x3BA55D);
  private static final Color DOWN = new Color(0xED4245);
  private static final Color FLAT = new Color(0x9AA0A6);
  private static final Color ASSIST = new Color(0x3BC9DB);
  private static final Color CLIMB_BAND = new Color(255, 215, 0, 38);
  private static final Color CLIMB_SWATCH = new Color(0xC9A227);

  /// Vertical speed the ladder climb assist forces: (0.2 - gravity 0.08) * drag 0.98.
  private static final double LADDER_ASSIST_SPEED = 0.1176;
  private static final double ASSIST_EPSILON = 0.005;

  private JumpGraphRenderer() {
  }

  public static BufferedImage render(List<JumpSample> samples) {
    var image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    var g = image.createGraphics();

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    g.setColor(BACKGROUND);
    g.fillRect(0, 0, WIDTH, HEIGHT);

    var plotX = MARGIN_LEFT;
    var plotY = MARGIN_TOP;
    var plotW = WIDTH - MARGIN_LEFT - MARGIN_RIGHT;
    var plotH = HEIGHT - MARGIN_TOP - MARGIN_BOTTOM;

    g.setColor(PLOT_BACKGROUND);
    g.fillRect(plotX, plotY, plotW, plotH);

    if (samples.isEmpty()) {
      g.setColor(TEXT);
      g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
      g.drawString("No jump data", plotX + 16, plotY + 28);
      g.dispose();
      return image;
    }

    var n = samples.size();

    // Normalize Y so the first sample (the ground the jump started from) is 0.
    var base = samples.get(0).positionY();
    var height = new double[n];
    for (var i = 0; i < n; i++) {
      height[i] = samples.get(i).positionY() - base;
    }

    var rawMin = Double.MAX_VALUE;
    var rawMax = -Double.MAX_VALUE;
    for (var h : height) {
      rawMin = Math.min(rawMin, h);
      rawMax = Math.max(rawMax, h);
    }

    var yMin = Math.min(0.0, rawMin);
    var yMax = Math.max(0.0, rawMax);
    var pad = (yMax - yMin) * 0.08;
    if (pad == 0) {
      pad = 0.1;
    }
    yMin -= pad;
    yMax += pad;

    var finalYMin = yMin;
    var finalYMax = yMax;

    // Climb bands: shade each maximal run of consecutive on-climbable ticks, from the first
    // climb tick's point to the last one's.
    g.setColor(CLIMB_BAND);
    var runStart = -1;
    for (var i = 0; i <= n; i++) {
      var climbing = i < n && samples.get(i).onClimbable();
      if (climbing && runStart < 0) {
        runStart = i;
      } else if (!climbing && runStart >= 0) {
        var left = xFor(runStart, n, plotX, plotW);
        var right = xFor(i - 1, n, plotX, plotW);
        g.fillRect((int) Math.round(left), plotY, (int) Math.round(right - left), plotH);
        runStart = -1;
      }
    }

    // Horizontal gridlines + y-axis labels at nice steps.
    g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    var step = niceStep(yMax - yMin, 6);
    for (var v = Math.ceil(yMin / step) * step; v <= yMax + 1e-9; v += step) {
      var y = yFor(v, finalYMin, finalYMax, plotY, plotH);
      g.setColor(GRID);
      g.drawLine(plotX, (int) Math.round(y), plotX + plotW, (int) Math.round(y));
      g.setColor(TEXT);
      var label = "%.3f".formatted(Math.abs(v) < 1e-9 ? 0.0 : v);
      var lw = g.getFontMetrics().stringWidth(label);
      g.drawString(label, plotX - 8 - lw, (int) Math.round(y) + 4);
    }

    // Emphasized baseline at the start height (0).
    var zeroY = yFor(0, finalYMin, finalYMax, plotY, plotH);
    g.setColor(ZERO_LINE);
    g.setStroke(new BasicStroke(1.5f));
    g.drawLine(plotX, (int) Math.round(zeroY), plotX + plotW, (int) Math.round(zeroY));

    // X-axis tick labels (thinned out when there are many ticks).
    g.setColor(TEXT);
    var labelEvery = Math.max(1, (int) Math.ceil(n / 16.0));
    for (var i = 0; i < n; i++) {
      if (i % labelEvery != 0 && i != n - 1) {
        continue;
      }
      var x = xFor(i, n, plotX, plotW);
      var label = String.valueOf(samples.get(i).tick());
      var lw = g.getFontMetrics().stringWidth(label);
      g.setColor(AXIS);
      g.drawLine((int) Math.round(x), plotY + plotH, (int) Math.round(x), plotY + plotH + 4);
      g.setColor(TEXT);
      g.drawString(label, (int) Math.round(x) - lw / 2, plotY + plotH + 18);
    }

    // Curve: neutral connecting line, points colored by rise/fall direction.
    g.setStroke(new BasicStroke(2f));
    g.setColor(CURVE);
    for (var i = 0; i < n - 1; i++) {
      var x1 = xFor(i, n, plotX, plotW);
      var y1 = yFor(height[i], finalYMin, finalYMax, plotY, plotH);
      var x2 = xFor(i + 1, n, plotX, plotW);
      var y2 = yFor(height[i + 1], finalYMin, finalYMax, plotY, plotH);
      g.draw(new Line2D.Double(x1, y1, x2, y2));
    }
    g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
    var metrics = g.getFontMetrics();
    for (var i = 0; i < n; i++) {
      var x = xFor(i, n, plotX, plotW);
      var y = yFor(height[i], finalYMin, finalYMax, plotY, plotH);
      var vy = i == 0 ? 0 : height[i] - height[i - 1];
      var direction = i == 0 ? 0 : Double.compare(height[i], height[i - 1]);

      // The ladder climb assist forces the tick to move at exactly the climb speed while on a
      // climbable - mark those ticks distinctly (vs ballistic pass-through or a capped fall).
      var assisted = i > 0 && samples.get(i).onClimbable()
          && Math.abs(vy - LADDER_ASSIST_SPEED) < ASSIST_EPSILON;

      g.setColor(assisted ? ASSIST : direction > 0 ? UP : direction < 0 ? DOWN : FLAT);
      g.fill(new Ellipse2D.Double(x - 3.5, y - 3.5, 7, 7));

      // Label each point with its per-tick vy (the height delta). The tick-0 baseline has none.
      if (i == 0) {
        continue;
      }
      var label = "%+.3f".formatted(vy);
      var lx = x - metrics.stringWidth(label) / 2.0;
      var ly = y - 9;
      if (ly < plotY + 11) {
        ly = y + 20;
      }
      g.setColor(TEXT);
      g.drawString(label, (int) Math.round(lx), (int) Math.round(ly));
    }

    // Plot border.
    g.setColor(AXIS);
    g.setStroke(new BasicStroke(1f));
    g.drawRect(plotX, plotY, plotW, plotH);

    // Title + axis captions.
    var totalTicks = samples.get(n - 1).tick();
    g.setColor(TEXT);
    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
    g.drawString("Jump — %d ticks, peak height %.3f".formatted(totalTicks, rawMax),
        plotX, MARGIN_TOP - 24);

    g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    g.setColor(AXIS);
    g.drawString("y offset (blocks)", plotX, MARGIN_TOP - 8);
    var tickCaption = "tick";
    g.drawString(tickCaption, plotX + plotW - g.getFontMetrics().stringWidth(tickCaption),
        HEIGHT - 12);

    drawLegend(g, plotX + plotW, MARGIN_TOP - 26);

    g.dispose();
    return image;
  }

  /// A compact right-aligned legend for the two ladder-related markers.
  private static void drawLegend(Graphics2D g, int rightX, int y) {
    g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    var fm = g.getFontMetrics();

    var labels = new String[] {"ladder assist", "on ladder"};
    var colors = new Color[] {ASSIST, CLIMB_SWATCH};

    var totalWidth = 0;
    for (var label : labels) {
      totalWidth += 12 + 5 + fm.stringWidth(label) + 18;
    }
    totalWidth -= 18;

    var x = rightX - totalWidth;
    for (var i = 0; i < labels.length; i++) {
      g.setColor(colors[i]);
      g.fillRect(x, y - 9, 11, 11);
      x += 12 + 5;
      g.setColor(TEXT);
      g.drawString(labels[i], x, y);
      x += fm.stringWidth(labels[i]) + 18;
    }
  }

  private static double xFor(int index, int n, int plotX, int plotW) {
    if (n <= 1) {
      return plotX + plotW / 2.0;
    }
    return plotX + (double) plotW * index / (n - 1);
  }

  private static double yFor(double value, double yMin, double yMax, int plotY, int plotH) {
    return plotY + plotH * (yMax - value) / (yMax - yMin);
  }

  /// A "nice" round step (1/2/5 x 10^k) that splits {@code range} into roughly {@code target} steps.
  private static double niceStep(double range, int target) {
    if (range <= 0) {
      return 1;
    }
    var raw = range / target;
    var magnitude = Math.pow(10, Math.floor(Math.log10(raw)));
    var normalized = raw / magnitude;
    double niceNormalized;
    if (normalized < 1.5) {
      niceNormalized = 1;
    } else if (normalized < 3) {
      niceNormalized = 2;
    } else if (normalized < 7) {
      niceNormalized = 5;
    } else {
      niceNormalized = 10;
    }
    return niceNormalized * magnitude;
  }
}
