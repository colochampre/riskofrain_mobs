package io.github.colochampre.riskofrain_mobs.events;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.ChatEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.utils.Env;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import io.github.colochampre.riskofrain_mobs.registry.RoRNetwork;
import io.github.colochampre.riskofrain_mobs.registry.RoRSounds;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import io.github.colochampre.riskofrain_mobs.entities.allies.AbstractDroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModEvents {

  private static Difficulty lastDifficulty = null;
  private static final Map<UUID, Integer> playerLevels = new HashMap<>();

  public static void registerEvents() {
    var configs = RoRConfigs.get();

    ChatEvent.RECEIVED.register((player, message) -> {
      if (configs.SOUNDS.CHAT_MESSAGE > 0) {
        sendPacket(player, RoRSounds.CHAT_MESSAGE.get(), configs.SOUNDS.CHAT_MESSAGE);
      }
      return EventResult.pass();
    });

    // Tamed drones breaking down instead of dying is handled in AbstractDroneEntity.die
    EntityEvent.LIVING_DEATH.register((entity, source) -> {
      if (entity instanceof Player player && configs.SOUNDS.PLAYER_DEATH_SOUND > 0) {
        player.level().playSound(null, player.blockPosition(), RoRSounds.PLAYER_DEATH.get(),
                player.getSoundSource(), (float) configs.SOUNDS.PLAYER_DEATH_SOUND / 100, 1.0F);
      }
      return EventResult.pass();
    });

    EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
      if (entity instanceof AbstractDroneEntity drone) {
        Entity attacker = source.getEntity();
        if (attacker instanceof Player && !drone.isTame()) {
          return EventResult.interruptFalse();
        }
      }
      return EventResult.pass();
    });

    LifecycleEvent.SERVER_STARTED.register(server -> {
      lastDifficulty = server.overworld().getDifficulty();
    });

    PlayerEvent.PLAYER_ADVANCEMENT.register((player, advancement) -> {
      if (configs.SOUNDS.ADVANCEMENT > 0) {
        sendPacket(player, RoRSounds.ADVANCEMENT.get(), configs.SOUNDS.ADVANCEMENT);
      }
    });

    PlayerEvent.PLAYER_QUIT.register(player -> playerLevels.remove(player.getUUID()));

    TickEvent.SERVER_POST.register(server -> {
      Difficulty currentDifficulty = server.overworld().getDifficulty();
      if (lastDifficulty != null && currentDifficulty != lastDifficulty) {
        playDifficultyChangeSound(server);
        lastDifficulty = currentDifficulty;
      }
      for (ServerPlayer player : server.getPlayerList().getPlayers()) {
        int currentLevel = player.experienceLevel;
        int lastLevel = playerLevels.getOrDefault(player.getUUID(), currentLevel);
        if (currentLevel > lastLevel && configs.SOUNDS.LEVEL_UP > 0) {
          sendPacket(player, RoRSounds.LEVEL_UP.get(), configs.SOUNDS.LEVEL_UP);
        }
        playerLevels.put(player.getUUID(), currentLevel);
      }
    });
  }

  private static void playDifficultyChangeSound(MinecraftServer server) {
    int volume = RoRConfigs.get().SOUNDS.DIFFICULTY_UPDATE;
    if (volume <= 0) return;

    // One buffer per packet: a shared one is consumed by the first reader (e.g. the integrated server's local player)
    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
      sendPacket(player, RoRSounds.DIFFICULTY_CHANGE.get(), volume);
    }
  }

  private static void sendPacket(ServerPlayer player, SoundEvent sound, int volume) {
    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
    buf.writeResourceLocation(sound.getLocation());
    buf.writeFloat((float) volume / 100);
    buf.writeFloat(1.0F);
    NetworkManager.sendToPlayer(player, RoRNetwork.PLAY_SOUND_S2C, buf);
  }

  @Environment(EnvType.CLIENT)
  public static void registerClientEvents() {
  }

  public static void init() {
    registerEvents();
    StoneGolemSummoning.register();
    if (Platform.getEnvironment() == Env.CLIENT) {
      registerClientEvents();
    }
  }
}
