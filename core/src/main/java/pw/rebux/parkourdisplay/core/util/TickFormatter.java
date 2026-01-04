package pw.rebux.parkourdisplay.core.util;

import java.util.concurrent.TimeUnit;

public final class TickFormatter {

  public static String formatTicks(long ticks) {
    var negative = ticks < 0;
    var total = Math.abs(ticks * 1000 / 20);
    var minutes = total / 60000;
    var seconds = (total / 1000) % 60;
    var millis = total % 1000;
    var unit = minutes > 0 ? TimeUnit.MINUTES : TimeUnit.SECONDS;

    var builder = new StringBuilder();
    if (negative) builder.append("-");
    if (unit == TimeUnit.MINUTES) builder.append(String.format("%02d.", minutes));
    builder.append(String.format("%02d.", seconds));
    builder.append(String.format("%03d", millis));
    return builder.toString();
  }
}
