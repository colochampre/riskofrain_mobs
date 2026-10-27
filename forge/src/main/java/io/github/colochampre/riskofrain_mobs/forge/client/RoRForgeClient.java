package io.github.colochampre.riskofrain_mobs.forge.client;

import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.client.models.*;
import io.github.colochampre.riskofrain_mobs.client.renderer.*;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityRendering;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RoRMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RoRForgeClient {

  // FMLClientSetupEvent runs mods in parallel, and EntityRenderers.register writes to an unsynchronized map
  @SubscribeEvent
  public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(RoREntityTypes.BEETLE.get(), BeetleRenderer::new);
    event.registerEntityRenderer(RoREntityTypes.LEMURIAN.get(), LemurianRenderer::new);
    event.registerEntityRenderer(RoREntityTypes.STONE_GOLEM.get(), StoneGolemRenderer::new);
    event.registerEntityRenderer(RoREntityTypes.WISP.get(), WispRenderer::new);
    event.registerEntityRenderer(RoREntityTypes.GUNNER_TURRET.get(), GunnerTurretRenderer::new);
    event.registerEntityRenderer(RoREntityTypes.GUNNER_DRONE.get(), GunnerDroneRenderer::new);
    event.registerEntityRenderer(RoREntityTypes.DRONE_BULLET_ENTITY.get(), BulletRenderer::new);
  }

  @SubscribeEvent
  public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
    event.registerLayerDefinition(RoREntityRendering.BEETLE_LAYER, BeetleModel::createBodyLayer);
    event.registerLayerDefinition(RoREntityRendering.LEMURIAN_LAYER, LemurianModel::createBodyLayer);
    event.registerLayerDefinition(RoREntityRendering.STONE_GOLEM_LAYER, StoneGolemModel::createBodyLayer);
    event.registerLayerDefinition(RoREntityRendering.WISP_LAYER, WispModel::createBodyLayer);
    event.registerLayerDefinition(RoREntityRendering.GUNNER_TURRET_LAYER, GunnerTurretModel::createBodyLayer);
    event.registerLayerDefinition(RoREntityRendering.GUNNER_DRONE_LAYER, GunnerDroneModel::createBodyLayer);
  }
}
