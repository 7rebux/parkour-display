package pw.rebux.parkourdisplay.core.macro;

import lombok.RequiredArgsConstructor;
import net.labymod.api.event.Phase;
import net.labymod.api.event.Priority;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.render.GameRenderEvent;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

/// Forwards LabyMod tick and render events to the shared macro engine. The macro engine handles
/// both tick phases (stepping only on the pre phase) and the sub-tick rotation interpolation.
@RequiredArgsConstructor
public final class MacroListener {

  private final ParkourDisplayAddon addon;

  @Subscribe(Priority.LATE)
  public void onGameTick(GameTickEvent event) {
    this.addon.macroEngine().onTick(event.phase() == Phase.POST);
  }

  @Subscribe
  public void onGameRender(GameRenderEvent event) {
    if (event.phase() != Phase.PRE) {
      return;
    }

    this.addon.macroEngine().onRenderFrame(this.addon.labyAPI().minecraft().getPartialTicks());
  }
}
