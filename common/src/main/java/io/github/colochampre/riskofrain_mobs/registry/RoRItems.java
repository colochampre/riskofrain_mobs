package io.github.colochampre.riskofrain_mobs.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class RoRItems {

  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(RoRMod.MOD_ID, Registries.ITEM);

  public static RegistrySupplier<Item> GUNNER_TURRET_ITEM;

  public static void init() {
    GUNNER_TURRET_ITEM = registerItem("gunner_turret_item", () ->
            new Item(baseProperties("gunner_turret_item").arch$tab(CreativeModeTabs.SPAWN_EGGS)));

    ITEMS.register();
  }

  public static RegistrySupplier<Item> registerItem(String name, Supplier<Item> item) {
    return ITEMS.register(new ResourceLocation(RoRMod.MOD_ID, name), item);
  }

  public static Item.Properties baseProperties(String name) {
    return new Item.Properties();
  }
}
