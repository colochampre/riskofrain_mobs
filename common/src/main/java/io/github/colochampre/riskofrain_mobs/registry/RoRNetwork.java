package io.github.colochampre.riskofrain_mobs.registry;

import io.github.colochampre.riskofrain_mobs.RoRMod;
import net.minecraft.resources.ResourceLocation;

public class RoRNetwork {
  // The S2C receiver lives in client.RoRModClient so the dedicated server never loads client classes
  public static final ResourceLocation PLAY_SOUND_S2C = new ResourceLocation(RoRMod.MOD_ID, "play_sound_s2c");
}
