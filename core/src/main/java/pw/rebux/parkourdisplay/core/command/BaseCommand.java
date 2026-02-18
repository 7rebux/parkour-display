package pw.rebux.parkourdisplay.core.command;

import static net.labymod.api.client.component.Component.space;
import static net.labymod.api.client.component.Component.text;
import static net.labymod.api.client.component.Component.translatable;

import java.util.List;
import net.labymod.api.client.chat.command.Command;
import net.labymod.api.client.chat.command.SubCommand;
import net.labymod.api.client.component.format.NamedTextColor;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.command.landingblock.AddLandingBlockCommand;
import pw.rebux.parkourdisplay.core.command.landingblock.ListLandingBlocksCommand;
import pw.rebux.parkourdisplay.core.command.landingblock.RemoveLandingBlockCommand;
import pw.rebux.parkourdisplay.core.command.landingblock.ResetLandingBlockCommand;
import pw.rebux.parkourdisplay.core.command.macro.ListMacrosCommand;
import pw.rebux.parkourdisplay.core.command.macro.RunMacroCommand;
import pw.rebux.parkourdisplay.core.command.macro.SaveMacroCommand;
import pw.rebux.parkourdisplay.core.command.run.AddRunSplitCommand;
import pw.rebux.parkourdisplay.core.command.run.ClearRunCommand;
import pw.rebux.parkourdisplay.core.command.run.ListRunFiles;
import pw.rebux.parkourdisplay.core.command.run.LoadRunCommand;
import pw.rebux.parkourdisplay.core.command.run.RemoveRunSplitCommand;
import pw.rebux.parkourdisplay.core.command.run.ResetRunSplitsCommand;
import pw.rebux.parkourdisplay.core.command.run.SaveRunCommand;
import pw.rebux.parkourdisplay.core.command.run.SetRunEndCommand;
import pw.rebux.parkourdisplay.core.command.run.SetRunStartCommand;

public final class BaseCommand extends Command {

  private static final String translationKeyBase = "parkourdisplay.commands.%s.messages";

  private final ParkourDisplayAddon addon;

  public BaseCommand(ParkourDisplayAddon addon) {
    // Could add cyv and mpk alias here, but maybe users want to use one of them
    // additionally to ParkourDisplay.
    super("parkourdisplay", "pd");

    this.addon = addon;

    this.messagePrefix(ParkourDisplayAddon.MESSAGE_PREFIX);

    // Landing block commands
    this.addSubCommand(new AddLandingBlockCommand(addon));
    this.addSubCommand(new ListLandingBlocksCommand(addon));
    this.addSubCommand(new RemoveLandingBlockCommand(addon));
    this.addSubCommand(new ResetLandingBlockCommand(addon));

    // Macro commands
    this.addSubCommand(new ListMacrosCommand(addon));
    this.addSubCommand(new RunMacroCommand(addon));
    this.addSubCommand(new SaveMacroCommand(addon));

    // Run commands
    this.addSubCommand(new SetRunStartCommand(addon));
    this.addSubCommand(new SetRunEndCommand(addon));
    this.addSubCommand(new AddRunSplitCommand(addon));
    this.addSubCommand(new RemoveRunSplitCommand(addon));
    this.addSubCommand(new ResetRunSplitsCommand(addon));
    this.addSubCommand(new SaveRunCommand(addon));
    this.addSubCommand(new LoadRunCommand(addon));
    this.addSubCommand(new ListRunFiles(addon));
    this.addSubCommand(new ClearRunCommand(addon));
  }

  @Override
  public boolean execute(String prefix, String[] arguments) {
    addon.displayMessage(
        text()
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

  private void addSubCommand(SubCommand command) {
    this.withSubCommand(
        command
            .messagePrefix(ParkourDisplayAddon.MESSAGE_PREFIX)
            .translationKey(translationKeyBase.formatted(command.getPrefix())));
  }
}
