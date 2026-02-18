package pw.rebux.parkourdisplay.core.command.run;

import java.util.Objects;
import net.labymod.api.client.chat.command.SubCommand;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.util.ChatMessage;

public final class SetRunStartCommand extends SubCommand {

  private final ParkourDisplayAddon addon;

  public SetRunStartCommand(ParkourDisplayAddon addon) {
    super("setstart", "ss");
    this.addon = addon;
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    var player = Objects.requireNonNull(this.addon.labyAPI().minecraft().getClientPlayer());

    this.addon.runState().startPosition(player.position().toDoubleVector3());
    ChatMessage.ofTranslatable(ChatMessage.commandKey(this, "success")).send();

    return true;
  }
}
