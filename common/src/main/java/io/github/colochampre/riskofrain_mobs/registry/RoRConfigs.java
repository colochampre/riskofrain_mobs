package io.github.colochampre.riskofrain_mobs.registry;

import io.github.colochampre.riskofrain_mobs.RoRMod;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = RoRMod.MOD_ID)
public class RoRConfigs implements ConfigData {

    @ConfigEntry.Gui.Excluded
    private static RoRConfigs INSTANCE;

    @ConfigEntry.Category("gunner_drone")
    @ConfigEntry.Gui.Tooltip
    public int GUNNER_DRONE_MAX_HEALTH = 20;

    @ConfigEntry.Category("gunner_drone")
    @ConfigEntry.Gui.Tooltip
    public double BULLETS_DAMAGE = 2.0;

    public static void init() {
        AutoConfig.register(RoRConfigs.class, GsonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(RoRConfigs.class).getConfig();
    }

    public static RoRConfigs get() {
        return INSTANCE;
    }
}
