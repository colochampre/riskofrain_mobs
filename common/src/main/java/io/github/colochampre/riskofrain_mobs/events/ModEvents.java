package io.github.colochampre.riskofrain_mobs.events;

import dev.architectury.platform.Platform;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.*;
import dev.architectury.utils.Env;
import io.github.colochampre.riskofrain_mobs.entities.allies.AbstractDroneEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class ModEvents {

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
