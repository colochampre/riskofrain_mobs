package io.github.colochampre.riskofrain_mobs.registry;

import dev.architectury.networking.NetworkManager;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;

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

          if (RoRConfigs.get().SOUNDS.DIFFICULTY_UPDATE > 0) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(RoRSounds.DIFFICULTY_CHANGE.get(),
                1.0F, RoRConfigs.get().SOUNDS.DIFFICULTY_UPDATE / 100.0F));
          }
        }
      });
    });
  }
}
