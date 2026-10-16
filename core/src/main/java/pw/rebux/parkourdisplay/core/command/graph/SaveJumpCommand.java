package pw.rebux.parkourdisplay.core.command.graph;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

public final class SaveJumpCommand extends SubCommand {

  private static final SimpleDateFormat FILE_TIMESTAMP =
      new SimpleDateFormat("yyyyMMdd-HHmmss");

  private final ParkourDisplayAddon addon;

  public SaveJumpCommand(ParkourDisplayAddon addon) {
    super("savejump", "jumpgraph");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var samples = this.addon.jumpRecorder().lastJump();

    if (samples.isEmpty()) {
      ChatMessage.of(this, "noJump")
          .withColor(NamedTextColor.RED)
          .send();
      return true;
    }

    var name = arguments.length > 0
        ? arguments[0]
        : "jump-" + FILE_TIMESTAMP.format(new Date());

    try {
      var file = this.addon.jumpGraphFileManager().save(samples, name);
      ChatMessage.of(this, "success")
          .withColor(NamedTextColor.GREEN)
          .withArgs(file.getAbsolutePath())
          .send();
    } catch (IOException e) {
      this.addon.logger().error("Failed to save jump graph.", e);
      ChatMessage.of(this, "error")
          .withColor(NamedTextColor.RED)
          .send();
    }

    return true;
  }
}
