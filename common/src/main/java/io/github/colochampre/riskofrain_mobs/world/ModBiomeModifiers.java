package io.github.colochampre.riskofrain_mobs.world;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.level.biome.BiomeModifications;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class ModBiomeModifiers {

  public static void init() {
    LifecycleEvent.SETUP.register(() -> {
      BiomeModifications.addProperties((ctx, mutable) -> {
        var keyOpt = ctx.getKey();
        if (keyOpt.isPresent()) {
          var configs = RoRConfigs.get().MOBS;
          ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, keyOpt.get());
          if (ctx.hasTag(BiomeTags.IS_OVERWORLD) && !key.equals(Biomes.DEEP_DARK) && !key.equals(Biomes.THE_VOID)) {
            int droneSpawnRate = configs.DRONES.DRONES_SPAWN_RATE;
            if (droneSpawnRate > 0) {
              mutable.getSpawnProperties().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(
                      RoREntityTypes.GUNNER_DRONE.get(), droneSpawnRate, 1, 1));
              mutable.getSpawnProperties().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(
                      RoREntityTypes.GUNNER_TURRET.get(), droneSpawnRate, 1, 1));
            }
            if (!key.equals(Biomes.MUSHROOM_FIELDS)) {
              if (configs.BEETLES.OVERWORLD_SPAWN_RATE > 0) {
                mutable.getSpawnProperties().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                        RoREntityTypes.BEETLE.get(), configs.BEETLES.OVERWORLD_SPAWN_RATE,
                        configs.BEETLES.MIN_GROUP_SIZE, configs.BEETLES.MAX_GROUP_SIZE));
              }
              if (configs.LEMURIANS.OVERWORLD_SPAWN_RATE > 0) {
                mutable.getSpawnProperties().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                        RoREntityTypes.LEMURIAN.get(), configs.LEMURIANS.OVERWORLD_SPAWN_RATE,
                        configs.LEMURIANS.MIN_GROUP_SIZE, configs.LEMURIANS.MAX_GROUP_SIZE));
              }
              if (configs.STONE_GOLEMS.OVERWORLD_SPAWN_RATE > 0) {
              /* mutable.getSpawnProperties().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                      RoREntityTypes.STONE_GOLEM.get(), configs.STONE_GOLEMS.OVERWORLD_SPAWN_RATE,
                       configs.STONE_GOLEMS.MIN_GROUP_SIZE, configs.STONE_GOLEMS.MAX_GROUP_SIZE)); */
              }
              if (configs.WISPS.OVERWORLD_SPAWN_RATE > 0) {
                mutable.getSpawnProperties().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                        RoREntityTypes.WISP.get(), configs.WISPS.OVERWORLD_SPAWN_RATE,
                        configs.WISPS.MIN_GROUP_SIZE, configs.WISPS.MAX_GROUP_SIZE));
              }
            }
          }
          if (ctx.hasTag(BiomeTags.IS_NETHER) && !key.equals(Biomes.THE_VOID)) {
            if (!key.equals(Biomes.WARPED_FOREST)) {
              if (configs.BEETLES.NETHER_SPAWN_RATE > 0) {
                mutable.getSpawnProperties().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                        RoREntityTypes.BEETLE.get(), configs.BEETLES.NETHER_SPAWN_RATE,
                        configs.BEETLES.MIN_GROUP_SIZE, configs.BEETLES.MAX_GROUP_SIZE));
              }
              if (configs.LEMURIANS.NETHER_SPAWN_RATE > 0) {
                mutable.getSpawnProperties().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                        RoREntityTypes.LEMURIAN.get(), configs.LEMURIANS.NETHER_SPAWN_RATE,
                        configs.LEMURIANS.MIN_GROUP_SIZE, configs.LEMURIANS.MAX_GROUP_SIZE));
              }
              if (configs.STONE_GOLEMS.NETHER_SPAWN_RATE > 0) {
              /* mutable.getSpawnProperties().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                      RoREntityTypes.STONE_GOLEM.get(), configs.STONE_GOLEMS.NETHER_SPAWN_RATE, 1, 1)); */
              }
              if (configs.WISPS.NETHER_SPAWN_RATE > 0) {
                mutable.getSpawnProperties().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
                        RoREntityTypes.WISP.get(), configs.WISPS.NETHER_SPAWN_RATE,
                        configs.WISPS.MIN_GROUP_SIZE, configs.WISPS.MAX_GROUP_SIZE));
              }
            }
          }
        }
      });
    });
  }
}
