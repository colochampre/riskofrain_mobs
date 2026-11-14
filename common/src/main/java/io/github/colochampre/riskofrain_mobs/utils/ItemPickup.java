package io.github.colochampre.riskofrain_mobs.utils;

import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Shared item pickup logic for the mobs that implement {@link InventoryCarrier}. The tag is referenced by id only,
 * so riskofrain_items stays an optional dependency: without it the tag is empty and nothing is picked up.
 */
public final class ItemPickup {

  public static final TagKey<Item> PASSIVE_ITEMS = TagKey.create(Registries.ITEM,
          new ResourceLocation("riskofrain_items", "passive_items"));

  private ItemPickup() {
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
