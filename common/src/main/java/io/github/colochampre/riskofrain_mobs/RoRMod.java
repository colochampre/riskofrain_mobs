package io.github.colochampre.riskofrain_mobs;

import io.github.colochampre.riskofrain_mobs.events.ModEvents;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
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
    ModBiomeModifiers.init();
    // Client-only setup runs from the platform client entrypoints via client.RoRModClient
  }
}
