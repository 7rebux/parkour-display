package pw.rebux.parkourdisplay.fabric;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// Entry point for the standalone Fabric build of ParkourDisplay.
///
/// The run-splits and macro logic lives in the platform-neutral `:common` module; this
/// initializer wires it to Fabric/Yarn APIs. Currently a scaffold that only proves the
/// module builds and loads.
public final class ParkourDisplayFabric implements ClientModInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger("ParkourDisplay");

  @Override
  public void onInitializeClient() {
    LOGGER.info("ParkourDisplay (Fabric) initialized");
  }
}
