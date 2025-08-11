package io.github.colochampre.riskofrain_mobs.registry;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.registry.level.biome.BiomeModifications;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class RoRBiomeModifiers {

  public static void init() {
    LifecycleEvent.SETUP.register(() -> {
      BiomeModifications.addProperties((ctx, mutable) -> {
        var keyOpt = ctx.getKey();

        if (keyOpt.isPresent() && ctx.hasTag(BiomeTags.IS_OVERWORLD)) {
          ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, keyOpt.get());

          if (!key.equals(Biomes.DEEP_DARK) && !key.equals(Biomes.THE_VOID)) {
            mutable.getSpawnProperties().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(
                    RoREntityTypes.GUNNER_DRONE.get(), RoRConfigs.get().MOBS.DRONES.DRONES_SPAWN_RATE, 1, 1));
          }
        }
      });
    });
  }
}
