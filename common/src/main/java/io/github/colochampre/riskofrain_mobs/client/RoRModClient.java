package io.github.colochampre.riskofrain_mobs.client;

import dev.architectury.networking.NetworkManager;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityRendering;
import io.github.colochampre.riskofrain_mobs.registry.RoRNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

/**
 * Client-only initialization. Must only be called from client entrypoints,
 * never from {@link io.github.colochampre.riskofrain_mobs.RoRMod#init()}.
 */
public final class RoRModClient {

  public static void init() {
    registerNetworkReceivers();
    RoREntityRendering.init();
  }

  private static void registerNetworkReceivers() {
    NetworkManager.registerReceiver(NetworkManager.Side.S2C, RoRNetwork.PLAY_SOUND_S2C, (buf, context) -> {
      ResourceLocation soundId = buf.readResourceLocation();
      float volume = buf.readFloat();
      float pitch = buf.readFloat();
      context.queue(() -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
          mc.getSoundManager().play(new SimpleSoundInstance(soundId, SoundSource.MASTER, volume, pitch, mc.player.getRandom(), false, 0, SimpleSoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true));
        }
      });
    });
  }
}
