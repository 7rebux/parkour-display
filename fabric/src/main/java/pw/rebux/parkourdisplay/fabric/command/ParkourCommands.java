package pw.rebux.parkourdisplay.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import pw.rebux.parkourdisplay.common.geom.Aabb;
import pw.rebux.parkourdisplay.common.geom.Vec3;
import pw.rebux.parkourdisplay.common.macro.MacroRotationChange;
import pw.rebux.parkourdisplay.common.macro.MacroTickState;
import pw.rebux.parkourdisplay.common.platform.Message;
import pw.rebux.parkourdisplay.common.platform.TextColorType;
import pw.rebux.parkourdisplay.common.run.RunSplit;
import pw.rebux.parkourdisplay.common.run.RunState;
import pw.rebux.parkourdisplay.common.run.RunTickState;
import pw.rebux.parkourdisplay.common.run.SplitBoxTriggerMode;
import pw.rebux.parkourdisplay.fabric.ParkourDisplayFabric;

/// Registers the `/pd` client command tree, delegating to the shared run/macro state and engines.
public final class ParkourCommands {

  private ParkourCommands() {
  }

  public static void register(
      CommandDispatcher<FabricClientCommandSource> dispatcher,
      ParkourDisplayFabric mod
  ) {
    dispatcher.register(ClientCommandManager.literal("pd")
        .then(ClientCommandManager.literal("setstart")
            .executes(c -> setStart(mod)))
        .then(ClientCommandManager.literal("setend")
            .executes(c -> setEnd(mod, c.getSource())))
        .then(ClientCommandManager.literal("addsplit")
            .executes(c -> addSplit(mod, 1.0))
            .then(ClientCommandManager.argument("size", DoubleArgumentType.doubleArg(0))
                .executes(c -> addSplit(mod, DoubleArgumentType.getDouble(c, "size") / 2))))
        .then(ClientCommandManager.literal("removesplit")
            .executes(c -> removeSplit(mod)))
        .then(ClientCommandManager.literal("resetsplits")
            .executes(c -> resetSplits(mod)))
        .then(ClientCommandManager.literal("clearrun")
            .executes(c -> clearRun(mod)))
        .then(ClientCommandManager.literal("savesplits")
            .then(ClientCommandManager.argument("name", StringArgumentType.word())
                .executes(c -> saveSplits(mod, StringArgumentType.getString(c, "name")))))
        .then(ClientCommandManager.literal("loadsplits")
            .then(ClientCommandManager.argument("name", StringArgumentType.word())
                .executes(c -> loadSplits(mod, StringArgumentType.getString(c, "name")))))
        .then(ClientCommandManager.literal("listsplitfiles")
            .executes(c -> listSplitFiles(mod, c.getSource())))
        .then(ClientCommandManager.literal("savemacro")
            .then(ClientCommandManager.argument("name", StringArgumentType.word())
                .executes(c -> saveMacro(mod, StringArgumentType.getString(c, "name")))))
        .then(ClientCommandManager.literal("runmacro")
            .then(ClientCommandManager.argument("name", StringArgumentType.word())
                .executes(c -> runMacro(mod, StringArgumentType.getString(c, "name")))))
        .then(ClientCommandManager.literal("listmacros")
            .executes(c -> listMacros(mod, c.getSource()))));
  }

  private static int setStart(ParkourDisplayFabric mod) {
    var run = mod.runState();
    var player = mod.context().player();
    if (run == null || player == null) {
      return 0;
    }
    var pos = player.position();
    run.startPosition(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
    send(mod, "commands.setstart.messages.success", TextColorType.GREEN);
    return 1;
  }

  private static int setEnd(ParkourDisplayFabric mod, FabricClientCommandSource source) {
    var run = mod.runState();
    var crosshair = source.getClient().crosshairTarget;

    if (!(crosshair instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
      send(mod, "commands.setend.messages.invalidBlock", TextColorType.RED);
      return 0;
    }

    var pos = hit.getBlockPos();
    // Finish box on the top face of the targeted block.
    var box = new Aabb(
        pos.getX(), pos.getY() + 1.0, pos.getZ(),
        pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0);
    run.endSplit(new RunSplit("Finish", box, SplitBoxTriggerMode.IntersectXZSameY));

    mod.context().chat().display(
        Message.of("commands.setend.messages.success").color(TextColorType.GREEN).arg("Ground"));
    return 1;
  }

  private static int addSplit(ParkourDisplayFabric mod, double offset) {
    var run = mod.runState();
    var player = mod.context().player();
    if (run == null || player == null) {
      return 0;
    }
    var pos = player.position();
    var box = new Aabb(
        pos.getX() - offset, pos.getY(), pos.getZ() - offset,
        pos.getX() + offset, pos.getY(), pos.getZ() + offset);
    run.splits().add(new RunSplit(
        "Split %d".formatted(run.splits().size() + 1), box, SplitBoxTriggerMode.IntersectXZSameY));
    send(mod, "commands.addsplit.messages.success", TextColorType.GREEN);
    return 1;
  }

  private static int removeSplit(ParkourDisplayFabric mod) {
    var run = mod.runState();
    if (run != null && !run.splits().isEmpty()) {
      run.splits().remove(run.splits().size() - 1);
    }
    send(mod, "commands.removesplit.messages.success", TextColorType.GREEN);
    return 1;
  }

  private static int resetSplits(ParkourDisplayFabric mod) {
    var run = mod.runState();
    if (run != null) {
      run.splits().forEach(split -> split.personalBest(null));
      if (run.endSplit() != null) {
        run.endSplit().personalBest(null);
      }
    }
    send(mod, "commands.resetsplits.messages.success", TextColorType.GREEN);
    return 1;
  }

  private static int clearRun(ParkourDisplayFabric mod) {
    var run = mod.runState();
    if (run != null) {
      run.reset();
      run.startPosition(null);
      run.endSplit(null);
      run.splits().clear();
    }
    send(mod, "commands.clearrun.messages.success", TextColorType.GREEN);
    return 1;
  }

  private static int saveSplits(ParkourDisplayFabric mod, String name) {
    try {
      mod.runRepository().save(mod.runState(), name);
      send(mod, "commands.savesplits.messages.success", TextColorType.GREEN);
    } catch (IOException e) {
      send(mod, "commands.savesplits.messages.error", TextColorType.RED);
    }
    return 1;
  }

  private static int loadSplits(ParkourDisplayFabric mod, String name) {
    var run = mod.runState();
    try {
      var data = mod.runRepository().load(name);
      run.reset();
      run.startPosition(data.start());
      run.endSplit(data.end());
      run.splits(data.splits());
      send(mod, "commands.loadsplits.messages.success", TextColorType.GREEN);
    } catch (FileNotFoundException e) {
      send(mod, "commands.loadsplits.messages.notFound", TextColorType.RED);
    }
    return 1;
  }

  private static int listSplitFiles(ParkourDisplayFabric mod, FabricClientCommandSource source) {
    var files = mod.runRepository().availableFiles();
    if (files.isEmpty()) {
      send(mod, "commands.listsplitfiles.messages.empty", TextColorType.RED);
      return 1;
    }
    files.forEach(name -> source.sendFeedback(Text.literal("- " + name)));
    return 1;
  }

  private static int saveMacro(ParkourDisplayFabric mod, String name) {
    var tickStates = mod.runState().previousTickStates();
    if (tickStates.isEmpty()) {
      send(mod, "commands.savemacro.messages.error", TextColorType.RED);
      return 0;
    }
    try {
      mod.macroRepository().save(accumulateInputs(mod, tickStates), name);
      send(mod, "commands.savemacro.messages.success", TextColorType.GREEN);
    } catch (IOException e) {
      send(mod, "commands.savemacro.messages.error", TextColorType.RED);
    }
    return 1;
  }

  private static int runMacro(ParkourDisplayFabric mod, String name) {
    try {
      mod.macroRunner().execute(mod.macroRepository().load(name));
    } catch (FileNotFoundException e) {
      send(mod, "commands.runmacro.messages.notFound", TextColorType.RED);
    }
    return 1;
  }

  private static int listMacros(ParkourDisplayFabric mod, FabricClientCommandSource source) {
    var files = mod.macroRepository().availableFiles();
    if (files.isEmpty()) {
      send(mod, "commands.listmacros.messages.empty", TextColorType.RED);
      return 1;
    }
    files.forEach(file -> source.sendFeedback(Text.literal("- " + file.name())));
    return 1;
  }

  /// Converts the recorded run tick history into macro frames, applying the configured absolute or
  /// relative rotation model (ported from the LabyMod SaveMacroCommand).
  private static ArrayList<MacroTickState> accumulateInputs(
      ParkourDisplayFabric mod,
      java.util.List<RunTickState> tickStates
  ) {
    var rotationChange = mod.context().config().rotationChange();
    var inputs = new ArrayList<MacroTickState>();
    var prevYaw = tickStates.get(0).position().yaw();
    var prevPitch = tickStates.get(0).position().pitch();

    for (var entry : tickStates) {
      inputs.add(new MacroTickState(
          entry.input().w(),
          entry.input().a(),
          entry.input().s(),
          entry.input().d(),
          entry.input().jump(),
          entry.input().sprint(),
          entry.input().sneak(),
          rotationChange == MacroRotationChange.Absolute
              ? entry.position().yaw()
              : entry.position().yaw() - prevYaw,
          rotationChange == MacroRotationChange.Absolute
              ? entry.position().pitch()
              : entry.position().pitch() - prevPitch
      ));

      prevYaw = entry.position().yaw();
      prevPitch = entry.position().pitch();
    }

    return inputs;
  }

  private static void send(ParkourDisplayFabric mod, String key, TextColorType color) {
    mod.context().chat().display(Message.of(key).color(color));
  }
}
