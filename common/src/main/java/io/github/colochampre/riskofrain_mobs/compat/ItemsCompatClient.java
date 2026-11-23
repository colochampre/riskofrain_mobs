package io.github.colochampre.riskofrain_mobs.compat;

import io.github.colochampre.riskofrain_mobs.RoRMod;

/**
 * Client-only part of the riskofrain_items integration. Only loaded by {@link ItemsCompat#initClient()}
 * after checking that riskofrain_items is present, so a dedicated server never loads it.
 */
final class ItemsCompatClient {
  private ItemsCompatClient() {
  }

  static void init() {
    RoRMod.LOGGER.debug("riskofrain_items found, enabling client carrier integration");
  }
}
