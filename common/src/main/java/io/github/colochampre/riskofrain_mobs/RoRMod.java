package io.github.colochampre.riskofrain_mobs;

import dev.architectury.event.events.client.ClientLifecycleEvent;
import io.github.colochampre.riskofrain_mobs.events.ModEvents;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import io.github.colochampre.riskofrain_mobs.registry.RoRNetwork;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityRendering;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;
import io.github.colochampre.riskofrain_mobs.registry.RoRItems;
import io.github.colochampre.riskofrain_mobs.world.ModBiomeModifiers;
import io.github.colochampre.riskofrain_mobs.registry.RoRSounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RoRMod {
  public static final String MOD_ID = "riskofrain_mobs";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  public static void init() {
    LOGGER.info("Initialized RoR Mod");

    RoRConfigs.init();
    RoRSounds.init();
    RoREntityTypes.init();
    RoRItems.init();
    ModEvents.init();
    RoRNetwork.init();
    ModBiomeModifiers.init();

    ClientLifecycleEvent.CLIENT_STARTED.register(listener -> {
      RoREntityRendering.init();
    });
  }
}
