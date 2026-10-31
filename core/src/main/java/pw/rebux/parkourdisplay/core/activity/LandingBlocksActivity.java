package pw.rebux.parkourdisplay.core.activity;

import net.labymod.api.client.component.Component;
import net.labymod.api.client.component.format.NamedTextColor;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.AutoActivity;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.activity.types.TitledActivity;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.ScrollWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.landingblock.LandingBlock;
import pw.rebux.parkourdisplay.core.landingblock.LandingBlockMode;

@AutoActivity
@Link("activity/landing-blocks.lss")
public final class LandingBlocksActivity extends TitledActivity {

  private final ParkourDisplayAddon addon;

  public LandingBlocksActivity(ParkourDisplayAddon addon) {
    super(Component.translatable("parkourdisplay.activities.landingBlocks.title"));
    this.addon = addon;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    var landingBlocks = this.addon.landingBlockRegistry().landingBlocks();

    if (landingBlocks.isEmpty()) {
      var empty = ComponentWidget.i18n(
          "parkourdisplay.activities.landingBlocks.empty", NamedTextColor.RED);
      empty.addId("empty");
      this.document.addChild(empty);
      return;
    }

    var list = new VerticalListWidget<FlexibleContentWidget>();
    list.addId("landing-block-list");

    for (var landingBlock : landingBlocks) {
      list.addChild(this.createEntry(list, landingBlock));
    }

    var scroll = new ScrollWidget(list);
    scroll.addId("landing-block-scroll");
    this.document.addChild(scroll);
  }

  private FlexibleContentWidget createEntry(
      VerticalListWidget<FlexibleContentWidget> list,
      LandingBlock landingBlock
  ) {
    var format = this.addon.configuration().decimalFormat();
    var box = landingBlock.collisionBox();

    var entry = new FlexibleContentWidget();
    entry.addId("landing-block-entry");

    var mode = ButtonWidget.component(this.modeComponent(landingBlock));
    mode.addId("column", "mode");
    mode.setPressable(() -> {
      var modes = LandingBlockMode.values();
      landingBlock.mode(modes[(landingBlock.mode().ordinal() + 1) % modes.length]);
      mode.updateComponent(this.modeComponent(landingBlock));
    });

    var reset = ButtonWidget.i18n(
        "parkourdisplay.activities.landingBlocks.reset", () -> landingBlock.bestDistance(null));
    reset.addId("reset-button");

    var landingBlocks = this.addon.landingBlockRegistry().landingBlocks();
    var remove = ButtonWidget.component(Component.text("X", NamedTextColor.RED), () -> {
      landingBlocks.remove(landingBlock);

      if (landingBlocks.isEmpty()) {
        this.reload();
      } else {
        list.removeChild(entry);
      }
    });
    remove.addId("remove-button");

    entry.addFlexibleContent(
        this.column("label", Component.text(landingBlock.label(), NamedTextColor.YELLOW)));
    entry.addContent(mode);
    entry.addContent(this.column("position", Component.text(
        "%s %s %s".formatted(
            String.format(format, box.getMinX()),
            String.format(format, box.getMinY()),
            String.format(format, box.getMinZ())),
        NamedTextColor.WHITE)));
    entry.addContent(reset);
    entry.addContent(remove);

    return entry;
  }

  private Component modeComponent(LandingBlock landingBlock) {
    return Component.text(landingBlock.mode().name(), NamedTextColor.GOLD);
  }

  private ComponentWidget column(String id, Component component) {
    var widget = ComponentWidget.component(component);
    widget.addId("column", id);
    return widget;
  }
}
