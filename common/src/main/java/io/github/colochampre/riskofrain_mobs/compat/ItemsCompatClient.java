package io.github.colochampre.riskofrain_mobs.compat;

import io.github.colochampre.riskofrain_items.client.CarriedItemsDisplay;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.renderer.BeetleRenderer;
import io.github.colochampre.riskofrain_mobs.client.renderer.GunnerTurretRenderer;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;
import net.minecraft.world.entity.Entity;

/**
 * Client-only part of the riskofrain_items integration. Only loaded by {@link ItemsCompat#initClient()}
 * after checking that riskofrain_items is present, so a dedicated server never loads it.
 */
final class ItemsCompatClient {
  private static final double WISP_UNLIT_SCALE = 1.15D;

  private ItemsCompatClient() {
  }

  static void init() {
    // The renderers scale the model, so the icons need the same scale to sit at the right height.
    // The values are the renderers' own constants. The entity types are passed as suppliers, resolved later by riskofrain_items.
    CarriedItemsDisplay.registerRenderScale(RoREntityTypes.BEETLE, entity -> BeetleRenderer.MODEL_SCALE);
    CarriedItemsDisplay.registerRenderScale(RoREntityTypes.GUNNER_TURRET, entity -> GunnerTurretRenderer.MODEL_SCALE);
    // A burning Wisp is wrapped in the vanilla fire overlay, which rises well above its hitbox: lift the icons over it.
    // Unlit (in water or rain), its mask reaches the top of the hitbox despite the model scale, so the icons go above the box.
    CarriedItemsDisplay.registerRenderScale(RoREntityTypes.WISP, entity -> entity.displayFireAnimation()
        ? Math.max(WISP_UNLIT_SCALE, flameTop(entity) / entity.getBbHeight())
        : WISP_UNLIT_SCALE);
    RoRMod.LOGGER.debug("riskofrain_items found, render scales registered");
  }

  /**
   * Height in blocks of the solid part of the vanilla fire overlay, following {@code EntityRenderDispatcher.renderFlame}:
   * layers 1.4 tall, each 0.45 higher than the last, one per 0.45 of height, all scaled by 1.4 times the width.
   * The top layer only adds thin flame tips, so it is left out: the icons may overlap the tips but not the fire.
   */
  private static double flameTop(Entity entity) {
    float scale = entity.getBbWidth() * 1.4F;
    int layers = (int) Math.ceil(entity.getBbHeight() / scale / 0.45F);
    return (1.4F + 0.45F * Math.max(layers - 2, 0)) * scale;
  }
}
