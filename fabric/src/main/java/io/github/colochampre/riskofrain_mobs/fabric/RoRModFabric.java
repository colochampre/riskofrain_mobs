package io.github.colochampre.riskofrain_mobs.fabric;

import net.fabricmc.api.ModInitializer;

import io.github.colochampre.riskofrain_mobs.RoRMod;

public final class RoRModFabric implements ModInitializer {
  @Override
  public void onInitialize() {
    // This code runs as soon as Minecraft is in a mod-load-ready state.
    // However, some things (like resources) may still be uninitialized.
    // Proceed with mild caution.

    // Run our common setup.
    RoRMod.init();
  }
}
