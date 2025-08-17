package io.github.colochampre.riskofrain_mobs.registry;

import dev.architectury.platform.Platform;
import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.models.GunnerDroneModel;
import io.github.colochampre.riskofrain_mobs.client.models.GunnerTurretModel;
import io.github.colochampre.riskofrain_mobs.client.models.LemurianModel;
import io.github.colochampre.riskofrain_mobs.client.renderer.GunnerDroneRenderer;
import io.github.colochampre.riskofrain_mobs.client.renderer.GunnerTurretRenderer;
import io.github.colochampre.riskofrain_mobs.client.renderer.BulletRenderer;
import io.github.colochampre.riskofrain_mobs.client.renderer.LemurianRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class RoREntityRendering {

  public static final ModelLayerLocation LEMURIAN_LAYER = new ModelLayerLocation(new ResourceLocation(RoRMod.MOD_ID, "lemurian_entity"), "main");
  public static final ModelLayerLocation GUNNER_TURRET_LAYER = new ModelLayerLocation(new ResourceLocation(RoRMod.MOD_ID, "gunner_turret_entity"), "main");
  public static final ModelLayerLocation GUNNER_DRONE_LAYER = new ModelLayerLocation(new ResourceLocation(RoRMod.MOD_ID, "gunner_drone_entity"), "main");

  public static void init() {
    if (Platform.isFabric()) {
      // Renderer Registries
      EntityRendererRegistry.register(RoREntityTypes.LEMURIAN, LemurianRenderer::new);
      EntityRendererRegistry.register(RoREntityTypes.GUNNER_TURRET, GunnerTurretRenderer::new);
      EntityRendererRegistry.register(RoREntityTypes.GUNNER_DRONE, GunnerDroneRenderer::new);
      EntityRendererRegistry.register(RoREntityTypes.DRONE_BULLET_ENTITY, BulletRenderer::new);
      // Layer Definitions
      EntityModelLayerRegistry.register(LEMURIAN_LAYER, LemurianModel::createBodyLayer);
      EntityModelLayerRegistry.register(GUNNER_TURRET_LAYER, GunnerTurretModel::createBodyLayer);
      EntityModelLayerRegistry.register(GUNNER_DRONE_LAYER, GunnerDroneModel::createBodyLayer);
    }
  }
}
