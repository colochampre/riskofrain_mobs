package io.github.colochampre.riskofrain_mobs.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class RoRSounds {

  public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(RoRMod.MOD_ID, Registries.SOUND_EVENT);

  
  public static RegistrySupplier<SoundEvent> DIFFICULTY_CHANGE = registerSound("event.difficulty_change");
  public static RegistrySupplier<SoundEvent> LEVEL_UP = registerSound("event.level_up");
  
  public static RegistrySupplier<SoundEvent> COIN_PROC = registerSound("interactive.coin.proc");
  public static RegistrySupplier<SoundEvent> INSUFFICIENT_FOUNDS = registerSound("interactive.insufficient_founds.proc");
  public static RegistrySupplier<SoundEvent> DRONE_BREAKS = registerSound("entity.drone.breaks");
  public static RegistrySupplier<SoundEvent> DRONE_DEATH  = registerSound("entity.drone.death");
  public static RegistrySupplier<SoundEvent> DRONE_FLYING = registerSound("entity.drone.flying");
  public static RegistrySupplier<SoundEvent> DRONE_REPAIR = registerSound("entity.drone.repair");
  public static RegistrySupplier<SoundEvent> DRONE_SHOOT  = registerSound("entity.drone.bullet_shoot");

  private static RegistrySupplier<SoundEvent> registerSound(String name) {
    return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(RoRMod.MOD_ID, name)));
  }

  public static void init() {
    SOUNDS.register();
  }
}
