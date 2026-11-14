package io.github.colochampre.riskofrain_mobs.utils;

import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared item pickup logic for the mobs that implement {@link InventoryCarrier}. The tag is referenced by id only,
 * so riskofrain_items stays an optional dependency: without it the tag is empty and nothing is picked up.
 */
public final class ItemPickup {

  public static final TagKey<Item> PASSIVE_ITEMS = TagKey.create(Registries.ITEM,
          new ResourceLocation("riskofrain_items", "passive_items"));

  // Keys of the synced summary: {Items: [{id: "<item id>", Count: <total>}, ...]}, empty when nothing is carried
  public static final String SUMMARY_ITEMS = "Items";
  public static final String SUMMARY_ID = "id";
  public static final String SUMMARY_COUNT = "Count";

  private ItemPickup() {
  }

  // Each entity class needs its own accessor, as defineId is keyed by the class that calls it
  public static EntityDataAccessor<CompoundTag> defineCarriedItems(Class<? extends Entity> owner) {
    return SynchedEntityData.defineId(owner, EntityDataSerializers.COMPOUND_TAG);
  }

  public static void registerCarriedItems(Entity entity, EntityDataAccessor<CompoundTag> accessor) {
    entity.getEntityData().define(accessor, new CompoundTag());
  }

  // The summary is rebuilt only when the container changes (pickup, load, drops), never per tick. SynchedEntityData
  // already skips the packet when the new summary equals the old one.
  public static SimpleContainer createInventory(Mob mob, EntityDataAccessor<CompoundTag> accessor) {
    SimpleContainer inventory = createInventory();
    inventory.addListener(changed -> {
      if (!mob.level().isClientSide) {
        mob.getEntityData().set(accessor, summarize(changed));
      }
    });
    return inventory;
  }

  // Total count per distinct item, in the order each item first appears in the container
  private static CompoundTag summarize(Container container) {
    Map<Item, Integer> totals = new LinkedHashMap<>();
    for (int i = 0; i < container.getContainerSize(); i++) {
      ItemStack stack = container.getItem(i);
      if (!stack.isEmpty()) {
        totals.merge(stack.getItem(), stack.getCount(), Integer::sum);
      }
    }
    CompoundTag summary = new CompoundTag();
    if (totals.isEmpty()) {
      return summary;
    }
    ListTag list = new ListTag();
    totals.forEach((item, count) -> {
      CompoundTag entry = new CompoundTag();
      entry.putString(SUMMARY_ID, BuiltInRegistries.ITEM.getKey(item).toString());
      entry.putInt(SUMMARY_COUNT, count);
      list.add(entry);
    });
    summary.put(SUMMARY_ITEMS, list);
    return summary;
  }

  // Read once per entity at construction: a mob keeps the size it was created with until it is reloaded
  public static SimpleContainer createInventory() {
    return new SimpleContainer(RoRConfigs.get().ITEM_PICKUP.INVENTORY_SLOTS);
  }

  public static boolean enemiesCanPickUp() {
    return RoRConfigs.get().ITEM_PICKUP.ENEMIES_CAN_PICK_UP;
  }

  public static boolean dronesCanPickUp() {
    return RoRConfigs.get().ITEM_PICKUP.DRONES_CAN_PICK_UP;
  }

  public static boolean wantsToPickUp(InventoryCarrier carrier, ItemStack stack) {
    return stack.is(PASSIVE_ITEMS) && carrier.getInventory().canAddItem(stack);
  }

  public static void pickUp(Mob mob, InventoryCarrier carrier, ItemEntity itemEntity) {
    InventoryCarrier.pickUpItem(mob, carrier, itemEntity);
  }

  public static boolean hasItems(InventoryCarrier carrier) {
    return !carrier.getInventory().isEmpty();
  }

  public static void save(InventoryCarrier carrier, CompoundTag tag) {
    carrier.writeInventoryToTag(tag);
  }

  // Vanilla SimpleContainer.fromTag silently loses what does not fit when the slot setting was lowered, so the
  // overflow is dropped at the mob's position instead
  public static void load(Mob mob, InventoryCarrier carrier, CompoundTag tag) {
    SimpleContainer inventory = carrier.getInventory();
    // Loading can run again on a live mob (/data merge), so start empty as vanilla fromTag does, or items double
    inventory.clearContent();
    ListTag list = tag.getList(InventoryCarrier.TAG_INVENTORY, 10);
    for (int i = 0; i < list.size(); i++) {
      ItemStack stack = ItemStack.of(list.getCompound(i));
      if (stack.isEmpty()) {
        continue;
      }
      ItemStack rest = inventory.addItem(stack);
      if (!rest.isEmpty() && !mob.level().isClientSide) {
        mob.spawnAtLocation(rest);
      }
    }
  }

  public static void dropAll(Mob mob, InventoryCarrier carrier) {
    if (mob.level().isClientSide) {
      return;
    }
    for (ItemStack stack : carrier.getInventory().removeAllItems()) {
      mob.spawnAtLocation(stack);
    }
  }

  // Hostile mobs are discarded in Peaceful by checkDespawn, which would delete whatever they carry
  public static void dropBeforePeacefulDespawn(Mob mob, InventoryCarrier carrier) {
    if (mob.level().getDifficulty() == Difficulty.PEACEFUL) {
      dropAll(mob, carrier);
    }
  }
}
