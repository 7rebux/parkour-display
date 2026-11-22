package pw.rebux.parkourdisplay.core.protocol;

import org.mcpk.protocol.core.ProtocolLogger;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@RequiredArgsConstructor
final class ParkourProtocolLogger implements ProtocolLogger {

  private final ParkourDisplayAddon addon;

  @Override
  public void info(String message) {
    this.addon.logger().info(message);
  }

  @Override
  public void warn(String message) {
    this.addon.logger().warn(message);
  }

  @Override
  public void error(String message, @Nullable Throwable throwable) {
    if (throwable == null) {
      this.addon.logger().error(message);
    } else {
      this.addon.logger().error(message, throwable);
    }
  }
}
