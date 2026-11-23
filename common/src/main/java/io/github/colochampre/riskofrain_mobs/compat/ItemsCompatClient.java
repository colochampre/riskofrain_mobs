package io.github.colochampre.riskofrain_mobs.compat;

import io.github.colochampre.riskofrain_items.client.CarriedItemsDisplay;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.renderer.BeetleRenderer;
import io.github.colochampre.riskofrain_mobs.client.renderer.GunnerTurretRenderer;
import io.github.colochampre.riskofrain_mobs.client.renderer.WispRenderer;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;

/**
 * Client-only part of the riskofrain_items integration. Only loaded by {@link ItemsCompat#initClient()}
 * after checking that riskofrain_items is present, so a dedicated server never loads it.
 */
final class ItemsCompatClient {
  private ItemsCompatClient() {
  }

  static void init() {
    // The renderers scale the model, so the icons need the same scale to sit at the right height.
    // The values are the renderers' own constants. The entity types are passed as suppliers, resolved later by riskofrain_items.
    CarriedItemsDisplay.registerRenderScale(RoREntityTypes.BEETLE, entity -> BeetleRenderer.MODEL_SCALE);
    CarriedItemsDisplay.registerRenderScale(RoREntityTypes.GUNNER_TURRET, entity -> GunnerTurretRenderer.MODEL_SCALE);
    CarriedItemsDisplay.registerRenderScale(RoREntityTypes.WISP, entity -> WispRenderer.MODEL_SCALE);
    RoRMod.LOGGER.debug("riskofrain_items found, render scales registered");
  }
}
