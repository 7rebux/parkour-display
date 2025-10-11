package pw.rebux.parkourdisplay.core.command;

import static net.labymod.api.client.component.Component.space;
import static net.labymod.api.client.component.Component.text;
import static net.labymod.api.client.component.Component.translatable;

import net.labymod.api.client.chat.command.Command;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextDecoration;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

public class BaseCommand extends Command {

  private final ParkourDisplayAddon addon;

  public BaseCommand(ParkourDisplayAddon addon) {
    // Could add cyv and mpk alias here, but maybe users want to use one of them
    // additionally to ParkourDisplay.
    super("parkourdisplay", "pd");

    this.addon = addon;

    // Landing block commands
    this.withSubCommand(new AddLandingBlockCommand(addon));
    this.withSubCommand(new ListLandingBlocksCommand(addon));
    this.withSubCommand(new RemoveLandingBlockCommand(addon));
    this.withSubCommand(new ResetLandingBlockCommand(addon));

    // Macro commands
    this.withSubCommand(new ReloadMacrosCommand(addon));
    this.withSubCommand(new ListMacrosCommand(addon));
    this.withSubCommand(new RunMacroCommand(addon));
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    addon.displayMessage(
        text()
            .append(text("ParkourDisplay", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .append(space())
            .append(text("Commands", NamedTextColor.LIGHT_PURPLE))
            .append(text(":", NamedTextColor.GRAY))
            .append(space())
            .build());

    this.getSubCommands().forEach(command -> {
      var name = command.getPrefix();

      addon.displayMessage(
          text()
              .append(text("-", NamedTextColor.GRAY))
              .append(space())
              .append(text(name, NamedTextColor.YELLOW))
              .append(text(":", NamedTextColor.GRAY))
              .append(space())
              .append(
                  translatable(
                      "parkourdisplay.commands.%s.description".formatted(name),
                      NamedTextColor.GOLD))
              .build());
    });

    return true;
  }
}
