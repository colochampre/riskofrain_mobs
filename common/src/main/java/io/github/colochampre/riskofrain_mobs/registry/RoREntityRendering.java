package io.github.colochampre.riskofrain_mobs.registry;

import dev.architectury.platform.Platform;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.entities.client.models.GunnerTurretModel;
import io.github.colochampre.riskofrain_mobs.entities.client.renderer.GunnerTurretRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class RoREntityRendering {

  public static final ModelLayerLocation GUNNER_TURRET_LAYER = new ModelLayerLocation(new ResourceLocation(RoRMod.MOD_ID, "gunner_turret_entity"), "main");

  public static void init() {
    if (Platform.isFabric()) {
      EntityRendererRegistry.register(RoREntityTypes.GUNNER_TURRET, GunnerTurretRenderer::new);
      EntityModelLayerRegistry.register(GUNNER_TURRET_LAYER, GunnerTurretModel::createBodyLayer);
    }
  }
}
