package io.github.colochampre.riskofrain_mobs.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class RoRItems {

  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(RoRMod.MOD_ID, Registries.ITEM);

  //public static RegistrySupplier<Item> GUNNER_TURRET_ITEM;
  public static final Map<DyeColor, RegistrySupplier<Item>> COLORED_GUNNER_TURRETS = new EnumMap<>(DyeColor.class);

  public static void init() {
    // GUNNER_TURRET_ITEM = registerItem("gunner_turret", () -> new Item(baseProperties("gunner_turret").stacksTo(16).arch$tab(CreativeModeTabs.SPAWN_EGGS)));
    // Registry ítems by color
    for (DyeColor color : DyeColor.values()) {
      if (color != DyeColor.LIGHT_BLUE) {
        String name = "gunner_turret_" + color.getName();
        RegistrySupplier<Item> coloredItem = registerItem(name, () -> new Item(baseProperties(name).stacksTo(16).arch$tab(CreativeModeTabs.SPAWN_EGGS)));
        COLORED_GUNNER_TURRETS.put(color, coloredItem);
      } else {
        RegistrySupplier<Item> defaultItem = registerItem("gunner_turret", () -> new Item(baseProperties("gunner_turret").stacksTo(16).arch$tab(CreativeModeTabs.SPAWN_EGGS)));
        COLORED_GUNNER_TURRETS.put(color, defaultItem);
      }
    }

    ITEMS.register();
  }

  public static RegistrySupplier<Item> registerItem(String name, Supplier<Item> item) {
    return ITEMS.register(new ResourceLocation(RoRMod.MOD_ID, name), item);
  }

  public static Item.Properties baseProperties(String name) {
    return new Item.Properties();
  }

  public static Item getTurretItemForColor(@Nullable DyeColor color) {
    return COLORED_GUNNER_TURRETS.get(color).get();
  }
}
