package io.github.colochampre.riskofrain_mobs.neoforge;

import io.github.colochampre.riskofrain_mobs.neoforge.config.NeoForgeConfigIntegration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.RoRModClient;

@Mod(RoRMod.MOD_ID)
public final class RoRModNeoForge {
  public RoRModNeoForge() {
    // Architectury finds the mod event bus on its own on NeoForge, so there is no EventBuses call.
    // Run our common setup.
    RoRMod.init();

    // Plain check instead of DistExecutor (deprecated on NeoForge): it loads the client classes only on the client
    if (FMLEnvironment.dist == Dist.CLIENT) {
      RoRModClient.init();
      NeoForgeConfigIntegration.register();
    }
  }
}
