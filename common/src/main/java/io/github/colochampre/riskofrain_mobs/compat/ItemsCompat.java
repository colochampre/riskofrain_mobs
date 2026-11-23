package io.github.colochampre.riskofrain_mobs.compat;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.colochampre.riskofrain_items.carrier.CarriedItems;
import io.github.colochampre.riskofrain_items.carrier.CarrierRules;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.entities.allies.AbstractDroneEntity;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.function.Predicate;

/**
 * Optional integration with riskofrain_items. This package is the only place that may reference
 * riskofrain_items classes, and only the public methods of this class may be called from the rest of the mod:
 * they check that riskofrain_items is loaded before touching any of its classes.
 */
public final class ItemsCompat {
  public static final String ITEMS_MOD_ID = "riskofrain_items";

  private ItemsCompat() {
  }

  public static boolean isLoaded() {
    return Platform.isModLoaded(ITEMS_MOD_ID);
  }

  /** Common setup. Safe to call without riskofrain_items. */
  public static void init() {
    if (!isLoaded()) {
      return;
    }
    Impl.init();
  }

  /** Client-only setup. Safe to call without riskofrain_items. */
  public static void initClient() {
    if (!isLoaded()) {
      return;
    }
    ItemsCompatClient.init();
  }

  /**
   * Drops everything the mob carries, for mobs that leave the world without dying (a drone breaking down, a turret
   * picked up as an item). Does nothing without riskofrain_items, where nothing is ever carried.
   */
  public static void dropCarriedItems(Mob mob) {
    if (!isLoaded()) {
      return;
    }
    Impl.dropCarriedItems(mob);
  }

  /** Holder for the code that references riskofrain_items classes; only loaded when the mod is present. */
  private static final class Impl {
    static void init() {
      // The settings are read when a mob is checked, so a change in the config screen applies without a restart
      Predicate<Mob> enemies = mob -> RoRConfigs.get().ITEM_PICKUP.ENEMIES_CAN_PICK_UP;
      // Untamed drones are inert shop items
      Predicate<Mob> drones = mob -> mob instanceof AbstractDroneEntity drone && drone.isTame()
              && RoRConfigs.get().ITEM_PICKUP.DRONES_CAN_PICK_UP;

      register(RoREntityTypes.BEETLE, enemies);
      register(RoREntityTypes.LEMURIAN, enemies);
      register(RoREntityTypes.STONE_GOLEM, enemies);
      register(RoREntityTypes.WISP, enemies);
      register(RoREntityTypes.GUNNER_DRONE, drones);
      register(RoREntityTypes.GUNNER_TURRET, drones);
      RoRMod.LOGGER.debug("riskofrain_items found, carrier rules registered");
    }

    // The entity types are deferred: on Forge they only exist after the registry event, later than the mod constructor
    private static void register(RegistrySupplier<? extends EntityType<?>> type, Predicate<Mob> rule) {
      type.listen(entityType -> CarrierRules.register(entityType, rule));
    }

    static void dropCarriedItems(Mob mob) {
      CarriedItems.dropAll(mob);
    }
  }
}
