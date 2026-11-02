package io.github.colochampre.riskofrain_mobs.world;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.List;
import java.util.function.BiConsumer;

public class ModBiomeModifiers {

  private record EnemySpawn(EntityType<?> entityType, int spawnRate, int minGroupSize, int maxGroupSize) {
  }

  public static void init() {
    LifecycleEvent.SETUP.register(ModBiomeModifiers::register);
  }

  // Architectury cannot read a biome's existing spawns on Fabric (SpawnProperties.getSpawners() returns null there),
  // so each platform registers the spawns with its own biome API
  @ExpectPlatform
  public static void register() {
    throw new AssertionError();
  }

  public static int totalWeight(List<MobSpawnSettings.SpawnerData> spawners) {
    return spawners.stream().mapToInt(spawner -> spawner.getWeight().asInt()).sum();
  }

  public static void addSpawns(ResourceKey<Biome> key, boolean isOverworld, boolean isNether, boolean hasCreatures,
                               int monsterWeight, BiConsumer<MobCategory, MobSpawnSettings.SpawnerData> addSpawn) {
    // Skip biomes without creatures of their own (rivers, badlands, snowy beaches): there every creature
    // spawn would be a drone, which piles them up along river banks
    if (isOverworld && hasCreatures && !key.equals(Biomes.DEEP_DARK) && !key.equals(Biomes.THE_VOID)) {
      addDrones(addSpawn);
    }
    if (isOverworld) {
      addEnemies(overworldEnemies(), monsterWeight, addSpawn);
    }
    if (isNether && !key.equals(Biomes.WARPED_FOREST)) {
      addEnemies(netherEnemies(), monsterWeight, addSpawn);
    }
  }

  private static void addDrones(BiConsumer<MobCategory, MobSpawnSettings.SpawnerData> addSpawn) {
    int droneSpawnRate = RoRConfigs.get().MOBS.DRONES.DRONES_SPAWN_RATE;
    if (droneSpawnRate > 0) {
      addSpawn.accept(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(
              RoREntityTypes.GUNNER_DRONE.get(), droneSpawnRate, 1, 1));
      addSpawn.accept(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(
              RoREntityTypes.GUNNER_TURRET.get(), droneSpawnRate, 1, 1));
    }
  }

  private static List<EnemySpawn> overworldEnemies() {
    var configs = RoRConfigs.get().MOBS;
    return List.of(
            new EnemySpawn(RoREntityTypes.BEETLE.get(), configs.BEETLES.OVERWORLD_SPAWN_RATE,
                    configs.BEETLES.MIN_GROUP_SIZE, configs.BEETLES.MAX_GROUP_SIZE),
            new EnemySpawn(RoREntityTypes.LEMURIAN.get(), configs.LEMURIANS.OVERWORLD_SPAWN_RATE,
                    configs.LEMURIANS.MIN_GROUP_SIZE, configs.LEMURIANS.MAX_GROUP_SIZE),
            new EnemySpawn(RoREntityTypes.STONE_GOLEM.get(), configs.STONE_GOLEMS.OVERWORLD_SPAWN_RATE,
                    configs.STONE_GOLEMS.MIN_GROUP_SIZE, configs.STONE_GOLEMS.MAX_GROUP_SIZE),
            new EnemySpawn(RoREntityTypes.WISP.get(), configs.WISPS.OVERWORLD_SPAWN_RATE,
                    configs.WISPS.MIN_GROUP_SIZE, configs.WISPS.MAX_GROUP_SIZE));
  }

  private static List<EnemySpawn> netherEnemies() {
    var configs = RoRConfigs.get().MOBS;
    return List.of(
            new EnemySpawn(RoREntityTypes.BEETLE.get(), configs.BEETLES.NETHER_SPAWN_RATE,
                    configs.BEETLES.MIN_GROUP_SIZE, configs.BEETLES.MAX_GROUP_SIZE),
            new EnemySpawn(RoREntityTypes.LEMURIAN.get(), configs.LEMURIANS.NETHER_SPAWN_RATE,
                    configs.LEMURIANS.MIN_GROUP_SIZE, configs.LEMURIANS.MAX_GROUP_SIZE),
            new EnemySpawn(RoREntityTypes.STONE_GOLEM.get(), configs.STONE_GOLEMS.NETHER_SPAWN_RATE, 1, 1),
            new EnemySpawn(RoREntityTypes.WISP.get(), configs.WISPS.NETHER_SPAWN_RATE,
                    configs.WISPS.MIN_GROUP_SIZE, configs.WISPS.MAX_GROUP_SIZE));
  }

  // Enemies never outweigh a biome's own monsters, so they share its spawns instead of taking them over. A biome whose
  // monsters weigh too little to give each enemy a weight of 1 gets none (mushroom fields and deep dark have no monsters)
  private static void addEnemies(List<EnemySpawn> enemies, int monsterWeight,
                                 BiConsumer<MobCategory, MobSpawnSettings.SpawnerData> addSpawn) {
    List<EnemySpawn> enabledEnemies = enemies.stream().filter(enemy -> enemy.spawnRate() > 0).toList();
    if (monsterWeight < enabledEnemies.size()) {
      return;
    }
    int enemyWeight = enabledEnemies.stream().mapToInt(EnemySpawn::spawnRate).sum();
    double scale = Math.min(1.0D, (double) monsterWeight / enemyWeight);
    for (EnemySpawn enemy : enabledEnemies) {
      int spawnRate = Math.max(1, (int) Math.round(enemy.spawnRate() * scale));
      addSpawn.accept(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(
              enemy.entityType(), spawnRate, enemy.minGroupSize(), enemy.maxGroupSize()));
    }
  }
}
