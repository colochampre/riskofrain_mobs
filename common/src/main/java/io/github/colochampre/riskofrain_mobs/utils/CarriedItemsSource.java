package io.github.colochampre.riskofrain_mobs.utils;

import net.minecraft.nbt.CompoundTag;

/**
 * An entity that exposes the summary of the items it carries to the client, see {@link ItemPickup#createInventory}.
 */
public interface CarriedItemsSource {

  CompoundTag getCarriedItemsTag();
}
