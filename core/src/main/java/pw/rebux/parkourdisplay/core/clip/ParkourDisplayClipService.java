package pw.rebux.parkourdisplay.core.clip;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import net.labymod.api.Laby;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.platform.launcher.ClipFolder;
import net.labymod.api.platform.launcher.ClipFolderIcon;
import net.labymod.api.platform.launcher.ClipFolderIcon.Background;
import net.labymod.api.platform.launcher.ClipFolderIcon.Block;
import net.labymod.api.util.concurrent.task.Task;
import pw.rebux.parkourdisplay.core.ParkourDisplayAddon;

@RequiredArgsConstructor
public class ParkourDisplayClipService {

  private static final ResourceLocation RUN =
      ResourceLocation.create(ParkourDisplayAddon.NAMESPACE, "run");

  private static final long CLIP_DELAY_SECONDS = 2;

  private final ParkourDisplayAddon addon;

  // Written from the launcher thread, read on the game thread.
  private volatile ClipFolder clipFolder;
  private volatile boolean requestingFolder;

  public void initialize() {
    this.requestFolder();
  }

  public void onRunFinish(long ticks, boolean newPB) {
    var clipService = Laby.references().clipService();

    if (!newPB || !this.addon.configuration().saveClips().get() || !clipService.isAvailable()) {
      return;
    }

    Task.builder(() -> clipService.requestClip(RUN, ticks + " ticks", ticks, this.clipFolder))
        .delay(CLIP_DELAY_SECONDS, TimeUnit.SECONDS)
        .build()
        .execute();

    // Clipping might not have been available on startup, so try again for the next clip.
    this.requestFolder();
  }

  private void requestFolder() {
    var clipService = Laby.references().clipService();

    if (this.clipFolder != null || this.requestingFolder || !clipService.isAvailable()) {
      return;
    }

    this.requestingFolder = true;
    clipService
        .createFolder("ParkourDisplay", ClipFolderIcon.block(Block.GOLD, Background.SKY))
        .whenComplete((folder, throwable) -> {
          if (folder != null) {
            this.clipFolder = folder;
          }
          this.requestingFolder = false;
        });
  }
}
