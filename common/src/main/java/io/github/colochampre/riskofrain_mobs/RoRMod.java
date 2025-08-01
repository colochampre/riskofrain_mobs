package io.github.colochampre.riskofrain_mobs;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityRendering;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;
import io.github.colochampre.riskofrain_mobs.registry.RoRItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RoRMod {
  public static final String MOD_ID = "riskofrain_mobs";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  public static void init() {
    // Write common init code here.
    LOGGER.info("Initialized RoR Mod");

    RoRItems.init();
    RoREntityTypes.init();

    ClientLifecycleEvent.CLIENT_STARTED.register(listener -> {
      RoREntityRendering.init();
    });
  }
}
