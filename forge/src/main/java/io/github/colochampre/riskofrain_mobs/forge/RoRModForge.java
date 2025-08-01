package io.github.colochampre.riskofrain_mobs.forge;

import dev.architectury.platform.forge.EventBuses;
import io.github.colochampre.riskofrain_mobs.entities.client.models.GunnerTurretModel;
import io.github.colochampre.riskofrain_mobs.entities.client.renderer.GunnerTurretRenderer;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityRendering;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import io.github.colochampre.riskofrain_mobs.RoRMod;

@Mod(RoRMod.MOD_ID)
public final class RoRModForge {
  public RoRModForge() {
    // Submit our event bus to let Architectury API register our content on the right time.
    EventBuses.registerModEventBus(RoRMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
    // Run our common setup.
    RoRMod.init();
  }

  @Mod.EventBusSubscriber(modid = RoRMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
  public static class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
      EntityRenderers.register(RoREntityTypes.GUNNER_TURRET.get(), GunnerTurretRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
      event.registerLayerDefinition(RoREntityRendering.GUNNER_TURRET_LAYER, GunnerTurretModel::createBodyLayer);
    }
  }
}
