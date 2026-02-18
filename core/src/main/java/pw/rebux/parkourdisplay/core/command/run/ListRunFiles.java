package pw.rebux.parkourdisplay.core.command.run;

import net.labymod.api.client.chat.command.SubCommand;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

public final class ListRunFiles extends SubCommand {

  private final ParkourDisplayAddon addon;

  public ListRunFiles(ParkourDisplayAddon addon) {
    super("listsplitfiles", "listrunfiles", "lsrun");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
      var files = this.addon.runFileManager().availableFiles();

      if (files.isEmpty()) {
        ChatMessage.ofTranslatable(ChatMessage.commandKey(this, "empty")).send();
        return true;
      }

      files.forEach(file -> {
        ChatMessage.ofTranslatable(ChatMessage.commandKey(this, "entry"))
            .withArgs(file.name(), file.type().name(), file.lastModified())
            .send();
      });

      return true;
    }
}
