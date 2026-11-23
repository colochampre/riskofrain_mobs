package io.github.colochampre.riskofrain_mobs.compat;

import dev.architectury.platform.Platform;
import io.github.colochampre.riskofrain_items.carrier.CarriedItems;
import io.github.colochampre.riskofrain_items.carrier.CarrierRules;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import io.github.colochampre.riskofrain_mobs.entities.allies.AbstractDroneEntity;
import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import io.github.colochampre.riskofrain_mobs.registry.RoREntityTypes;
import net.minecraft.world.entity.Mob;

import java.util.function.Predicate;

/**
 * Optional integration with riskofrain_items. This package is the only place that may reference
 * riskofrain_items classes, and only the public methods of this class may be called from the rest of the mod:
 * they check that riskofrain_items is loaded before touching any of its classes.
 */
public final class ItemsCompat {
  public static final String ITEMS_MOD_ID = "riskofrain_items";
  private static final String REQUIRED_ITEMS_VERSION = "2.1.0";

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
    try {
      Impl.init();
    } catch (LinkageError e) {
      logTooOld(e);
    }
  }

  /** Client-only setup. Safe to call without riskofrain_items. */
  public static void initClient() {
    if (!isLoaded()) {
      return;
    }
    try {
      ItemsCompatClient.init();
    } catch (LinkageError e) {
      logTooOld(e);
    }
  }

  /** An older riskofrain_items without the carrier API: keep running, mobs just do not pick up items. */
  private static void logTooOld(LinkageError e) {
    RoRMod.LOGGER.warn("riskofrain_items {} or newer is required for item pickup; the installed version has no carrier API, "
            + "so mobs will not pick up items", REQUIRED_ITEMS_VERSION, e);
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

      CarrierRules.register(RoREntityTypes.BEETLE, enemies);
      CarrierRules.register(RoREntityTypes.LEMURIAN, enemies);
      CarrierRules.register(RoREntityTypes.STONE_GOLEM, enemies);
      CarrierRules.register(RoREntityTypes.WISP, enemies);
      CarrierRules.register(RoREntityTypes.GUNNER_DRONE, drones);
      CarrierRules.register(RoREntityTypes.GUNNER_TURRET, drones);
      RoRMod.LOGGER.debug("riskofrain_items found, carrier rules registered");
    }

    static void dropCarriedItems(Mob mob) {
      CarriedItems.dropAll(mob);
    }
  }
}
