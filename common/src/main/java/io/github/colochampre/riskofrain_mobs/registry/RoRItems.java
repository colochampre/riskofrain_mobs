package io.github.colochampre.riskofrain_mobs.registry;

import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.colochampre.riskofrain_mobs.RoRMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class RoRItems {

  public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(RoRMod.MOD_ID, Registries.ITEM);

  // Spawn Eggs
  public static final RegistrySupplier<Item> GUNNER_TURRET_SPAWN_EGG = ITEMS.register("gunner_turret_spawn_egg", () ->
          new ArchitecturySpawnEggItem(RoREntityTypes.GUNNER_TURRET, 0x007ADA, 0xBCCCA8,
                  baseProperties("gunner_turret_spawn_egg", 64).arch$tab(CreativeModeTabs.SPAWN_EGGS)));

  public static final Map<DyeColor, RegistrySupplier<Item>> COLORED_GUNNER_TURRETS = createColoredTurretMap();

  private static Map<DyeColor, RegistrySupplier<Item>> createColoredTurretMap() {
    Map<DyeColor, RegistrySupplier<Item>> map = new EnumMap<>(DyeColor.class);
    for (DyeColor color : DyeColor.values()) {
      if (color != DyeColor.LIGHT_BLUE) {
        String name = "gunner_turret_" + color.getName(); // Example: "gunner_turret_red"
        map.put(color, registerItem(name, () -> new Item(baseProperties(name, 16).arch$tab(CreativeModeTabs.SPAWN_EGGS))));
      } else {
        map.put(color, registerItem("gunner_turret", () -> new Item(baseProperties("gunner_turret", 16).arch$tab(CreativeModeTabs.SPAWN_EGGS))));
      }
    }
    return map;
  }

  private static RegistrySupplier<Item> registerItem(String name, Supplier<Item> item) {
    return ITEMS.register(new ResourceLocation(RoRMod.MOD_ID, name), item);
  }

  private static Item.Properties baseProperties(String name, int stacksTo) {
    return new Item.Properties().stacksTo(stacksTo);
  }

  public static Item getTurretItemForColor(@Nullable DyeColor color) {
    if (color == null) {
      return COLORED_GUNNER_TURRETS.get(DyeColor.LIGHT_BLUE).get();
    }
    return COLORED_GUNNER_TURRETS.getOrDefault(color, COLORED_GUNNER_TURRETS.get(DyeColor.LIGHT_BLUE)).get();
  }

  public static void init() {
    ITEMS.register();
  }
}
