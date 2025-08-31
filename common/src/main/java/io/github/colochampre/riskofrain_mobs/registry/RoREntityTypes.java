package io.github.colochampre.riskofrain_mobs.registry;

import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.entities.allies.GunnerDroneEntity;
import io.github.colochampre.riskofrain_mobs.entities.allies.GunnerTurretEntity;
import io.github.colochampre.riskofrain_mobs.entities.enemies.BeetleEntity;
import io.github.colochampre.riskofrain_mobs.entities.enemies.LemurianEntity;
import io.github.colochampre.riskofrain_mobs.entities.projectiles.BulletEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.Supplier;

public class RoREntityTypes {

  private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(RoRMod.MOD_ID,
      Registries.ENTITY_TYPE);

  public static final RegistrySupplier<EntityType<GunnerTurretEntity>> GUNNER_TURRET = registerEntityType(
      "gunner_turret_entity", () -> EntityType.Builder.of(GunnerTurretEntity::new, MobCategory.CREATURE)
          .sized(0.8125F, 1.3125F)
          // .eyeHeight(1.15625F)
          .build(RoRMod.MOD_ID + ":gunner_turret_entity"));

  public static final RegistrySupplier<EntityType<GunnerDroneEntity>> GUNNER_DRONE = registerEntityType(
      "gunner_drone_entity", () -> EntityType.Builder.of(GunnerDroneEntity::new, MobCategory.CREATURE)
          .sized(0.75F, 1.15F)
          // .eyeHeight(0.055F)
          .build(RoRMod.MOD_ID + ":gunner_drone_entity"));

  public static final RegistrySupplier<EntityType<BeetleEntity>> BEETLE = registerEntityType(
          "beetle_entity", () -> EntityType.Builder.of(BeetleEntity::new, MobCategory.MONSTER)
                  .sized(0.98F, 1.70F)
                  // .eyeHeight(1.4375F)
                  .build(RoRMod.MOD_ID + ":beetle_entity"));

  public static final RegistrySupplier<EntityType<LemurianEntity>> LEMURIAN = registerEntityType(
          "lemurian_entity", () -> EntityType.Builder.of(LemurianEntity::new, MobCategory.MONSTER)
                  .sized(0.7F, 1.72F)
                  // .eyeHeight(1.62F)
                  .build(RoRMod.MOD_ID + ":lemurian_entity"));

  public static final RegistrySupplier<EntityType<BulletEntity>> DRONE_BULLET_ENTITY = registerEntityType("drone_bullet_entity", () ->
        EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build(RoRMod.MOD_ID + ":drone_bullet_entity"));

  public static void init() {
    ENTITIES.register();
    registerEntityAttributes();
    registerSpawnPlacement();
  }

  public static void registerEntityAttributes() {
    EntityAttributeRegistry.register(RoREntityTypes.BEETLE, BeetleEntity::createAttributes);
    EntityAttributeRegistry.register(RoREntityTypes.LEMURIAN, LemurianEntity::createAttributes);
    EntityAttributeRegistry.register(RoREntityTypes.GUNNER_TURRET, GunnerTurretEntity::createAttributes);
    EntityAttributeRegistry.register(RoREntityTypes.GUNNER_DRONE, GunnerDroneEntity::createAttributes);
  }

  public static void registerSpawnPlacement() {
    SpawnPlacementsRegistry.register(RoREntityTypes.BEETLE, SpawnPlacements.Type.ON_GROUND,
        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BeetleEntity::checkMonsterSpawnRules);
    SpawnPlacementsRegistry.register(RoREntityTypes.LEMURIAN, SpawnPlacements.Type.ON_GROUND,
        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LemurianEntity::checkMonsterSpawnRules);
    SpawnPlacementsRegistry.register(RoREntityTypes.GUNNER_TURRET, SpawnPlacements.Type.ON_GROUND,
        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, GunnerTurretEntity::checkDroneSpawnRules);
    SpawnPlacementsRegistry.register(RoREntityTypes.GUNNER_DRONE, SpawnPlacements.Type.ON_GROUND,
        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, GunnerDroneEntity::checkDroneSpawnRules);
  }

  private static <T extends Entity> RegistrySupplier<EntityType<T>> registerEntityType(String name, Supplier<EntityType<T>> entityType) {
    return ENTITIES.register(new ResourceLocation(RoRMod.MOD_ID, name), entityType);
  }
}
