package io.github.colochampre.riskofrain_mobs.world;

import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.function.BiConsumer;

public final class DroneSpawns {

  private DroneSpawns() {
  }

  // Architectury cannot read a biome's existing spawns on Fabric (SpawnProperties.getSpawners() returns null there),
  // so each platform registers drones with its own biome API
  @ExpectPlatform
  public static void register() {
    throw new AssertionError();
  }

  // Skip biomes without creatures of their own (rivers, badlands, snowy beaches): there every creature
  // spawn would be a drone, which piles them up along river banks
  public static boolean isEligible(ResourceKey<Biome> key, boolean isOverworld, boolean hasCreatures) {
    return isOverworld && hasCreatures && !key.equals(Biomes.DEEP_DARK) && !key.equals(Biomes.THE_VOID);
  }

  public static void addTo(BiConsumer<MobCategory, MobSpawnSettings.SpawnerData> addSpawn) {
    int droneSpawnRate = RoRConfigs.get().MOBS.DRONES.DRONES_SPAWN_RATE;
    if (droneSpawnRate > 0) {
      addSpawn.accept(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(
              RoREntityTypes.GUNNER_DRONE.get(), droneSpawnRate, 1, 1));
      addSpawn.accept(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(
              RoREntityTypes.GUNNER_TURRET.get(), droneSpawnRate, 1, 1));
    }
  }
}
