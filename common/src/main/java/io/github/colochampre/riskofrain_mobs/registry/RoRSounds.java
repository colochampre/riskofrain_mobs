package io.github.colochampre.riskofrain_mobs.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class RoRSounds {

  public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(RoRMod.MOD_ID, Registries.SOUND_EVENT);

  public static RegistrySupplier<SoundEvent> ADVANCEMENT = registerSound("event.advancement");
  public static RegistrySupplier<SoundEvent> CHAT_MESSAGE = registerSound("event.chat_message");
  public static RegistrySupplier<SoundEvent> DIFFICULTY_CHANGE = registerSound("event.difficulty_change");
  public static RegistrySupplier<SoundEvent> LEVEL_UP = registerSound("event.level_up");
  public static RegistrySupplier<SoundEvent> PLAYER_DEATH = registerSound("event.player_death");

  public static RegistrySupplier<SoundEvent> COIN_PROC = registerSound("interactive.coin.proc");
  public static RegistrySupplier<SoundEvent> INSUFFICIENT_FOUNDS = registerSound("interactive.insufficient_founds.proc");

  public static RegistrySupplier<SoundEvent> DRONE_BREAKS = registerSound("entity.drone.breaks");
  public static RegistrySupplier<SoundEvent> DRONE_DEATH = registerSound("entity.drone.death");
  public static RegistrySupplier<SoundEvent> DRONE_FLYING = registerSound("entity.drone.flying");
  public static RegistrySupplier<SoundEvent> DRONE_REPAIR = registerSound("entity.drone.repair");
  public static RegistrySupplier<SoundEvent> DRONE_SHOOT = registerSound("entity.drone.bullet_shoot");

  public static final RegistrySupplier<SoundEvent> BEETLE_AMBIENT = registerSound("entity.beetle.ambient");
  public static final RegistrySupplier<SoundEvent> BEETLE_ATTACK = registerSound("entity.beetle.attack");
  public static final RegistrySupplier<SoundEvent> BEETLE_DEATH = registerSound("entity.beetle.death");
  public static final RegistrySupplier<SoundEvent> BEETLE_HURT = registerSound("entity.beetle.hurt");
  public static final RegistrySupplier<SoundEvent> BEETLE_STEP = registerSound("entity.beetle.step");

  public static final RegistrySupplier<SoundEvent> LEMURIAN_AMBIENT = registerSound("entity.lemurian.ambient");
  public static final RegistrySupplier<SoundEvent> LEMURIAN_ATTACK = registerSound("entity.lemurian.attack");
  public static final RegistrySupplier<SoundEvent> LEMURIAN_DEATH = registerSound("entity.lemurian.death");
  public static final RegistrySupplier<SoundEvent> LEMURIAN_FIREBALL = registerSound("entity.lemurian.fireball");
  public static final RegistrySupplier<SoundEvent> LEMURIAN_HURT = registerSound("entity.lemurian.hurt");
  public static final RegistrySupplier<SoundEvent> LEMURIAN_STEP = registerSound("entity.lemurian.step");
  public static final RegistrySupplier<SoundEvent> LEMURIAN_SPAWN = registerSound("entity.lemurian.spawn");

  public static final RegistrySupplier<SoundEvent> STONE_GOLEM_CLAP = registerSound("entity.stone_golem.clap");
  public static final RegistrySupplier<SoundEvent> STONE_GOLEM_DEATH = registerSound("entity.stone_golem.death");
  public static final RegistrySupplier<SoundEvent> STONE_GOLEM_GROWL = registerSound("entity.stone_golem.growl");
  public static final RegistrySupplier<SoundEvent> STONE_GOLEM_HURT = registerSound("entity.stone_golem.hurt");
  public static final RegistrySupplier<SoundEvent> STONE_GOLEM_LASER_CHARGE = registerSound("entity.stone_golem.laser_charge");
  public static final RegistrySupplier<SoundEvent> STONE_GOLEM_LASER_FIRE = registerSound("entity.stone_golem.laser_fire");
  public static final RegistrySupplier<SoundEvent> STONE_GOLEM_SPAWN = registerSound("entity.stone_golem.spawn");
  public static final RegistrySupplier<SoundEvent> STONE_GOLEM_STEP = registerSound("entity.stone_golem.step");

  public static final RegistrySupplier<SoundEvent> WISP_AMBIENT = registerSound("entity.wisp.ambient");
  public static final RegistrySupplier<SoundEvent> WISP_ATTACK_CHARGE = registerSound("entity.wisp.attack_charge");
  public static final RegistrySupplier<SoundEvent> WISP_ATTACK_FIRE = registerSound("entity.wisp.attack_fire");
  public static final RegistrySupplier<SoundEvent> WISP_DEATH = registerSound("entity.wisp.death");
  public static final RegistrySupplier<SoundEvent> WISP_HURT = registerSound("entity.wisp.hurt");
  public static final RegistrySupplier<SoundEvent> WISP_LOOP = registerSound("entity.wisp.loop");
  public static final RegistrySupplier<SoundEvent> WISP_SPAWN = registerSound("entity.wisp.spawn");

  private static RegistrySupplier<SoundEvent> registerSound(String name) {
    return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(RoRMod.MOD_ID, name)));
  }

  public static void init() {
    SOUNDS.register();
  }
}
