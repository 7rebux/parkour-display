package pw.rebux.parkourdisplay.core.command.run;

import java.io.FileNotFoundException;
import net.labymod.api.client.chat.command.SubCommand;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

public final class LoadRunCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public LoadRunCommand(ParkourDisplayAddon addon) {
    super("loadsplits", "importsplits", "loadrun");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var runState = this.addon.runState();
    var landingBlockRegistry = this.addon.landingBlockRegistry();

    if (arguments.length == 0) {
      ChatMessage.ofTranslatable(ChatMessage.commandKey(this, "nameRequired")).send();
      return true;
    }

    var name = arguments[0];
    var isZM = arguments.length > 1 && arguments[1].equalsIgnoreCase("zm");

    try {
      var data = isZM
          ? this.addon.runFileManager().loadFromZM(name)
          : this.addon.runFileManager().load(name);

      runState.reset();
      runState.startPosition(data.start());
      runState.endSplit(data.end());
      runState.splits(data.splits());

      if (this.addon.configuration().importLandingBlocks().get()) {
        landingBlockRegistry.landingBlocks().addAll(data.landingBlocks());
      }

      ChatMessage.ofTranslatable(ChatMessage.commandKey(this, "success")).send();
    } catch (FileNotFoundException e) {
      ChatMessage.ofTranslatable(ChatMessage.commandKey(this, "notFound")).send();
    }

    return true;
  }
}
