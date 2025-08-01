package io.github.colochampre.riskofrain_mobs.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.entities.GunnerTurretEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public class RoREntityTypes {

  private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(RoRMod.MOD_ID, Registries.ENTITY_TYPE);

  public static RegistrySupplier<EntityType<GunnerTurretEntity>> GUNNER_TURRET;

  public static void init() {
    GUNNER_TURRET = registerEntityType("gunner_turret_entity", () -> EntityType.Builder.of(GunnerTurretEntity::new, MobCategory.CREATURE)
            .sized(0.8125F, 1.3125F)
            // .eyeHeight(1.15625F)
            .build(RoRMod.MOD_ID + ":gunner_turret_entity"));

    ENTITIES.register();
  }

  private static <T extends Entity> RegistrySupplier<EntityType<T>> registerEntityType(String name, Supplier<EntityType<T>> entityType){
    return ENTITIES.register(new ResourceLocation(RoRMod.MOD_ID, name), entityType);
  }
}
