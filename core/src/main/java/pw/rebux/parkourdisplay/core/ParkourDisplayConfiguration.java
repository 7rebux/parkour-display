package pw.rebux.parkourdisplay.core;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.property.ConfigProperty;

@ConfigName("settings")
@Accessors(fluent = true)
public class ParkourDisplayConfiguration extends AddonConfig {

  @SwitchSetting
  @Getter
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);
}
