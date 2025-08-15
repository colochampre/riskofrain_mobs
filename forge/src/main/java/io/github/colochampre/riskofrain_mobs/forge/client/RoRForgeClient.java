package io.github.colochampre.riskofrain_mobs.forge.client;

import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.models.GunnerDroneModel;
import io.github.colochampre.riskofrain_mobs.client.models.GunnerTurretModel;
import io.github.colochampre.riskofrain_mobs.client.renderer.BulletRenderer;
import io.github.colochampre.riskofrain_mobs.client.renderer.GunnerDroneRenderer;
import io.github.colochampre.riskofrain_mobs.client.renderer.GunnerTurretRenderer;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityRendering;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RoRMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RoRForgeClient {

  @SubscribeEvent
  public static void onClientSetup(FMLClientSetupEvent event) {
    EntityRenderers.register(RoREntityTypes.GUNNER_TURRET.get(), GunnerTurretRenderer::new);
    EntityRenderers.register(RoREntityTypes.GUNNER_DRONE.get(), GunnerDroneRenderer::new);
    EntityRenderers.register(RoREntityTypes.DRONE_BULLET_ENTITY.get(), BulletRenderer::new);
  }

  @SubscribeEvent
  public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
    event.registerLayerDefinition(RoREntityRendering.GUNNER_TURRET_LAYER, GunnerTurretModel::createBodyLayer);
    event.registerLayerDefinition(RoREntityRendering.GUNNER_DRONE_LAYER, GunnerDroneModel::createBodyLayer);
  }
}
