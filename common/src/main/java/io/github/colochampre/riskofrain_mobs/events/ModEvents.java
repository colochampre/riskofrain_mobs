package io.github.colochampre.riskofrain_mobs.events;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.utils.Env;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import io.github.colochampre.riskofrain_mobs.registry.RoRNetwork;
import io.github.colochampre.riskofrain_mobs.registry.RoRSounds;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import io.github.colochampre.riskofrain_mobs.entities.allies.AbstractDroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class ModEvents {

  private static Difficulty lastDifficulty = null;

  public static void registerEvents() {
    var configs = RoRConfigs.get();

    EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
      if (entity instanceof AbstractDroneEntity drone) {
        Entity attacker = source.getEntity();
        if (attacker instanceof Player && !drone.isTame()) {
          return EventResult.interruptFalse();
        }
      }
      return EventResult.pass();
    });

    EntityEvent.LIVING_DEATH.register((entity, source) -> {
      if (entity instanceof AbstractDroneEntity drone && drone.isTame()) {
        breakDrone(drone);
        return EventResult.interruptFalse();
      }
      if (entity instanceof Player player && configs.SOUNDS.PLAYER_DEATH_SOUND > 0) {
        player.level().playSound(null, player.blockPosition(), RoRSounds.PLAYER_DEATH.get(),
                player.getSoundSource(), (float) configs.SOUNDS.PLAYER_DEATH_SOUND / 100, 1.0F);
      }
      return EventResult.pass();
    });

    LifecycleEvent.SERVER_STARTED.register(server -> {
      lastDifficulty = server.overworld().getDifficulty();
    });

    TickEvent.SERVER_POST.register(server -> {
      Difficulty currentDifficulty = server.overworld().getDifficulty();
      if (lastDifficulty != null && currentDifficulty != lastDifficulty) {
        playDifficultyChangeSound(server);
        lastDifficulty = currentDifficulty;
      }
    });
  }

  private static void breakDrone(AbstractDroneEntity drone) {
    drone.setTame(false);
    drone.setOwnerUUID(null);
    drone.removeGoals();
    drone.setHealth(drone.getMaxHealth());
    drone.setCurrentGoldPrice(drone.getDronePrice());
    drone.setPriceName();
  }

  private static void playDifficultyChangeSound(MinecraftServer server) {
    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
      NetworkManager.sendToPlayer(player, RoRNetwork.PLAY_DIFFICULTY_CHANGE_SOUND, buf);
    }
  }

  @Environment(EnvType.CLIENT)
  public static void registerClientEvents() {
  }

  public static void init() {
    registerEvents();
    if (Platform.getEnvironment() == Env.CLIENT) {
      registerClientEvents();
    }
  }
}
