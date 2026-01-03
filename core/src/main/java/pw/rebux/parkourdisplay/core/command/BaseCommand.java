package pw.rebux.parkourdisplay.core.command;

import static net.labymod.api.client.component.Component.space;
import static net.labymod.api.client.component.Component.text;
import static net.labymod.api.client.component.Component.translatable;

import java.util.List;
import net.labymod.api.client.chat.command.Command;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.component.format.TextDecoration;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.command.lb.AddLandingBlockCommand;
import pw.rebux.parkourdisplay.core.command.lb.ListLandingBlocksCommand;
import pw.rebux.parkourdisplay.core.command.lb.RemoveLandingBlockCommand;
import pw.rebux.parkourdisplay.core.command.lb.ResetLandingBlockCommand;
import pw.rebux.parkourdisplay.core.command.macro.ListMacrosCommand;
import pw.rebux.parkourdisplay.core.command.macro.RunMacroCommand;
import pw.rebux.parkourdisplay.core.command.macro.SaveMacroCommand;
import pw.rebux.parkourdisplay.core.command.run.AddRunSplitCommand;
import pw.rebux.parkourdisplay.core.command.run.ListRunSplitFiles;
import pw.rebux.parkourdisplay.core.command.run.LoadRunSplitsCommand;
import pw.rebux.parkourdisplay.core.command.run.RemoveRunSplitCommand;
import pw.rebux.parkourdisplay.core.command.run.ResetRunSplitsCommand;
import pw.rebux.parkourdisplay.core.command.run.SaveRunSplitsCommand;
import pw.rebux.parkourdisplay.core.command.run.SetRunEndCommand;
import pw.rebux.parkourdisplay.core.command.run.SetRunStartCommand;

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
    this.withSubCommand(new ListMacrosCommand(addon));
    this.withSubCommand(new RunMacroCommand(addon));
    this.withSubCommand(new SaveMacroCommand(addon));

    // Run commands
    this.withSubCommand(new SetRunStartCommand(addon));
    this.withSubCommand(new SetRunEndCommand(addon));
    this.withSubCommand(new AddRunSplitCommand(addon));
    this.withSubCommand(new RemoveRunSplitCommand(addon));
    this.withSubCommand(new ResetRunSplitsCommand(addon));
    this.withSubCommand(new SaveRunSplitsCommand(addon));
    this.withSubCommand(new LoadRunSplitsCommand(addon));
    this.withSubCommand(new ListRunSplitFiles(addon));
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

  // Not working yet as stated on https://dev.labymod.net/pages/addon/features/commands/
  // Sad :(
  @Override
  public List<String> complete(String[] arguments) {
    if (arguments.length == 1) {
      return this.getSubCommands().stream().map(Command::getPrefix).toList();
    }

    return super.complete(arguments);
  }
}
