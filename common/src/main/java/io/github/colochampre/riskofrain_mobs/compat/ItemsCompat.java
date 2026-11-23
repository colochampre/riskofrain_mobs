package io.github.colochampre.riskofrain_mobs.compat;

import dev.architectury.platform.Platform;
import io.github.colochampre.riskofrain_mobs.RoRMod;

/**
 * Optional integration with riskofrain_items. This package is the only place that may reference
 * riskofrain_items classes, and only {@link #init()} and {@link #initClient()} may be called from the
 * rest of the mod: they check that riskofrain_items is loaded before touching any of its classes.
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

  /** Holder for the code that references riskofrain_items classes; only loaded when the mod is present. */
  private static final class Impl {
    static void init() {
      RoRMod.LOGGER.debug("riskofrain_items found, enabling carrier integration");
    }
  }
}
