package pw.rebux.parkourdisplay.core.widget;

import static net.labymod.api.client.component.Component.translatable;

import lombok.Getter;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;
import pw.rebux.parkourdisplay.core.widget.LastLandingBlockOffsetsWidget.LastLandingBlockOffsetsWidgetConfig;

public class LastLandingBlockOffsetsWidget extends TextHudWidget<LastLandingBlockOffsetsWidgetConfig> {

  private final ParkourDisplayAddon addon;

  private final TextLine[] textLines = new TextLine[3];

  public LastLandingBlockOffsetsWidget(ParkourDisplayAddon addon) {
    super("last_landing_block_offsets", LastLandingBlockOffsetsWidgetConfig.class);
    this.bindCategory(addon.category());
    this.addon = addon;
  }

  @Override
  public void load(LastLandingBlockOffsetsWidgetConfig config) {
    super.load(config);

    if (config.singleLine.get()) {
      this.textLines[0] = createLine(
          translatable("parkourdisplay.labels.last_landing_block_offsets.single"),
          "-");
    } else {
      this.textLines[0] = createLine(
          translatable("parkourdisplay.labels.last_landing_block_offsets.total"),
          "-");
      this.textLines[1] = createLine(
          translatable("parkourdisplay.labels.last_landing_block_offsets.x"),
          "-");
      this.textLines[2] = createLine(
          translatable("parkourdisplay.labels.last_landing_block_offsets.z"),
          "-");
    }
  }

  @Override
  public void onTick(boolean isEditorContext) {
    var stringFormat = "%%.%df".formatted(addon.configuration().offsetDecimalPlaces().get());
    var landingBlockRegistry = this.addon.landingBlockRegistry();
    var totalOffset = String.format(stringFormat, landingBlockRegistry.lastTotalLandingBlockOffset());
    var xOffset = String.format(stringFormat, landingBlockRegistry.lastLandingBlockOffsetX());
    var zOffset = String.format(stringFormat, landingBlockRegistry.lastLandingBlockOffsetZ());

    if (this.config.singleLine().get()) {
      this.textLines[0].updateAndFlush("%s %s %s".formatted(totalOffset, xOffset, zOffset));
    } else {
      this.textLines[0].updateAndFlush(totalOffset);
      this.textLines[1].updateAndFlush(xOffset);
      this.textLines[2].updateAndFlush(zOffset);
    }
  }

  @Getter
  public static class LastLandingBlockOffsetsWidgetConfig extends TextHudWidgetConfig {

    @SwitchSetting
    private final ConfigProperty<Boolean> singleLine = new ConfigProperty<>(false);
  }
}
