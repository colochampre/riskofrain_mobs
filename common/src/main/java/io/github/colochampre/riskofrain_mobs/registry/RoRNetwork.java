package io.github.colochampre.riskofrain_mobs.registry;

import dev.architectury.networking.NetworkManager;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

public class RoRNetwork {
  public static final ResourceLocation PLAY_DIFFICULTY_CHANGE_SOUND = new ResourceLocation(RoRMod.MOD_ID,
          "play_difficulty_change_sound");

  public static void init() {
    // S2C Receiver
    NetworkManager.registerReceiver(NetworkManager.Side.S2C, PLAY_DIFFICULTY_CHANGE_SOUND, (buf, context) -> {
      // This is executed on the client when the packet is received
      context.queue(() -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
          mc.level.playSound(mc.player, mc.player.blockPosition(), RoRSounds.DIFFICULTY_CHANGE.get(),
                  SoundSource.MASTER, 4.0F, 1.0F);
        }
      });
    });
  }
}
