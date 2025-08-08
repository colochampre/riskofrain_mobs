package io.github.colochampre.riskofrain_mobs.events;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.utils.Env;
import io.github.colochampre.riskofrain_mobs.registry.RoRNetwork;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import io.github.colochampre.riskofrain_mobs.entities.allies.AbstractDroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class ModEvents {

  private static Difficulty lastDifficulty = null;

  public static void events() {
    EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
      if (entity instanceof AbstractDroneEntity turret) {
        Entity attacker = source.getEntity();
        if (attacker instanceof Player && !turret.isTame()) {
          return EventResult.interruptFalse();
        }
      }
      return EventResult.pass();
    });

    LifecycleEvent.SERVER_STARTED.register(server -> {
      lastDifficulty = server.overworld().getDifficulty();
    });

    TickEvent.SERVER_POST.register(server -> {
      Difficulty currentDifficulty = server.overworld().getDifficulty();
      if (lastDifficulty != null && currentDifficulty != lastDifficulty) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
          NetworkManager.sendToPlayer(player, RoRNetwork.PLAY_DIFFICULTY_CHANGE_SOUND, buf);
        }
        lastDifficulty = currentDifficulty;
      }
    });
  }

  @Environment(EnvType.CLIENT)
  public static void clientEvents() {
  }

  public static void init() {
    events();
    if (Platform.getEnvironment() == Env.CLIENT) {
      clientEvents();
    }
  }
}
