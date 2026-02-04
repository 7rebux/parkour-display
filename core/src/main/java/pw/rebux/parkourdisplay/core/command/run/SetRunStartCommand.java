package pw.rebux.parkourdisplay.core.command.run;

import java.util.Objects;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

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
    this.displayTranslatable("success", NamedTextColor.GREEN);

    return true;
  }
}
