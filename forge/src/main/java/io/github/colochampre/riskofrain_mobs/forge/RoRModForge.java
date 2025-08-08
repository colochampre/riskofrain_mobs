package io.github.colochampre.riskofrain_mobs.forge;

import dev.architectury.platform.forge.EventBuses;
import io.github.colochampre.riskofrain_mobs.forge.config.RoRForgeConfigIntegration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import io.github.colochampre.riskofrain_mobs.RoRMod;

@Mod(RoRMod.MOD_ID)
public final class RoRModForge {
  public RoRModForge() {
    // Submit our event bus to let Architectury API register our content on the right time.
    EventBuses.registerModEventBus(RoRMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
    // Run our common setup.
    RoRMod.init();

    DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> RoRForgeConfigIntegration::register);
  }
}
