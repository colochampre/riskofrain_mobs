package io.github.colochampre.riskofrain_mobs.world.fabric;

import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.world.DroneSpawns;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;

public final class DroneSpawnsImpl {

  private DroneSpawnsImpl() {
  }

  public static void register() {
    BiomeModifications.create(new ResourceLocation(RoRMod.MOD_ID, "drone_spawns")).add(ModificationPhase.ADDITIONS,
            ctx -> DroneSpawns.isEligible(ctx.getBiomeKey(), ctx.hasTag(BiomeTags.IS_OVERWORLD),
                    !ctx.getBiome().getMobSettings().getMobs(MobCategory.CREATURE).isEmpty()),
            (ctx, mod) -> DroneSpawns.addTo(mod.getSpawnSettings()::addSpawn));
  }
}
