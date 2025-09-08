package io.github.colochampre.riskofrain_mobs.registry;

import dev.architectury.networking.NetworkManager;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class RoRNetwork {
  public static final ResourceLocation PLAY_SOUND_S2C = new ResourceLocation(RoRMod.MOD_ID, "play_sound_s2c");

  public static void init() {
    // S2C Receiver
    NetworkManager.registerReceiver(NetworkManager.Side.S2C, PLAY_SOUND_S2C, (buf, context) -> {
      ResourceLocation soundId = buf.readResourceLocation();
      float volume = buf.readFloat();
      float pitch = buf.readFloat();
      // This is executed on the client when the packet is received
      context.queue(() -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            mc.getSoundManager().play(new SimpleSoundInstance(soundId, SoundSource.MASTER, volume, pitch, mc.player.getRandom(), false, 0, SimpleSoundInstance.Attenuation.NONE, 0.0D, 0.0D, 0.0D, true));
        }
      });
    });
  }
}
