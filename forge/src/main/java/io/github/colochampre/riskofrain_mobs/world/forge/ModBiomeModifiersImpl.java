package io.github.colochampre.riskofrain_mobs.world.forge;

import dev.architectury.registry.level.biome.BiomeModifications;
import io.github.colochampre.riskofrain_mobs.world.ModBiomeModifiers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;

import java.util.List;

public final class ModBiomeModifiersImpl {

  private ModBiomeModifiersImpl() {
  }

  public static void register() {
    BiomeModifications.addProperties((ctx, mutable) -> ctx.getKey().ifPresent(id -> {
      var spawners = ctx.getProperties().getSpawnProperties().getSpawners();
      ModBiomeModifiers.addSpawns(ResourceKey.create(Registries.BIOME, id),
              ctx.hasTag(BiomeTags.IS_OVERWORLD), ctx.hasTag(BiomeTags.IS_NETHER),
              !spawners.getOrDefault(MobCategory.CREATURE, List.of()).isEmpty(),
              ModBiomeModifiers.totalWeight(spawners.getOrDefault(MobCategory.MONSTER, List.of())),
              mutable.getSpawnProperties()::addSpawn);
    }));
  }
}
