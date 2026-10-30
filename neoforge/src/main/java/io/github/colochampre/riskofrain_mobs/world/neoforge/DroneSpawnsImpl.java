package io.github.colochampre.riskofrain_mobs.world.neoforge;

import dev.architectury.registry.level.biome.BiomeModifications;
import io.github.colochampre.riskofrain_mobs.world.DroneSpawns;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;

import java.util.List;

public final class DroneSpawnsImpl {

  private DroneSpawnsImpl() {
  }

  public static void register() {
    BiomeModifications.addProperties((ctx, mutable) -> ctx.getKey().ifPresent(id -> {
      boolean hasCreatures = !ctx.getProperties().getSpawnProperties().getSpawners()
              .getOrDefault(MobCategory.CREATURE, List.of()).isEmpty();
      if (DroneSpawns.isEligible(ResourceKey.create(Registries.BIOME, id), ctx.hasTag(BiomeTags.IS_OVERWORLD), hasCreatures)) {
        DroneSpawns.addTo(mutable.getSpawnProperties()::addSpawn);
      }
    }));
  }
}
