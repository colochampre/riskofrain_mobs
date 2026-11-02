package io.github.colochampre.riskofrain_mobs.world.fabric;

import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.world.ModBiomeModifiers;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;

public final class ModBiomeModifiersImpl {

  private ModBiomeModifiersImpl() {
  }

  public static void register() {
    BiomeModifications.create(new ResourceLocation(RoRMod.MOD_ID, "mob_spawns")).add(ModificationPhase.ADDITIONS,
            BiomeSelectors.all(), (ctx, mod) -> {
              var spawnSettings = ctx.getBiome().getMobSettings();
              ModBiomeModifiers.addSpawns(ctx.getBiomeKey(),
                      ctx.hasTag(BiomeTags.IS_OVERWORLD), ctx.hasTag(BiomeTags.IS_NETHER),
                      !spawnSettings.getMobs(MobCategory.CREATURE).isEmpty(),
                      ModBiomeModifiers.totalWeight(spawnSettings.getMobs(MobCategory.MONSTER).unwrap()),
                      mod.getSpawnSettings()::addSpawn);
            });
  }
}
