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

    @ConfigEntry.Category("mobs")
    @ConfigEntry.Gui.TransitiveObject
    public MobsConfig MOBS = new MobsConfig();

    @ConfigEntry.Category("sounds")
    @ConfigEntry.Gui.TransitiveObject
    public SoundsConfig SOUNDS = new SoundsConfig();

    public static void init() {
        AutoConfig.register(RoRConfigs.class, GsonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(RoRConfigs.class).getConfig();
    }

    public static RoRConfigs get() {
        return INSTANCE;
    }

    public static class MobsConfig implements ConfigData {
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public DronesConfig DRONES = new DronesConfig();

        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public BeetlesConfig BEETLES = new BeetlesConfig();
    }

    public static class DronesConfig implements ConfigData {
        @ConfigEntry.Gui.Tooltip
        public double GUNNER_DRONE_MAX_HEALTH = 20;

        @ConfigEntry.Gui.Tooltip
        public double GUNNER_TURRET_MAX_HEALTH = 20;

        @ConfigEntry.Gui.Tooltip
        public double BULLETS_DAMAGE = 2.0;

        @ConfigEntry.Gui.Tooltip
        public int DRONES_SPAWN_RATE = 1; // 0 - 100
    }

    public static class BeetlesConfig implements ConfigData {
        @ConfigEntry.Gui.Tooltip
        public double BEETLE_MAX_HEALTH = 20.0;

        @ConfigEntry.Gui.Tooltip
        public double BEETLE_ATTACK_DAMAGE = 2.5;

        @ConfigEntry.Gui.Tooltip
        public int BEETLE_OVERWORLD_SPAWN_RATE = 66; // 0 - 100

        @ConfigEntry.Gui.Tooltip
        public int BEETLE_NETHER_SPAWN_RATE = 10; // 0 - 100

        @ConfigEntry.Gui.Tooltip
        public int BEETLE_MIN_GROUP_SIZE = 1; // 1 - 8

        @ConfigEntry.Gui.Tooltip
        public int BEETLE_MAX_GROUP_SIZE = 3; // 1 - 8
    }

    public static class SoundsConfig implements ConfigData {
        @ConfigEntry.Gui.Tooltip
        public int ADVANCEMENT = 0;

        @ConfigEntry.Gui.Tooltip
        public int CHAT_MESSAGE = 0;

        @ConfigEntry.Gui.Tooltip
        public int DIFFICULTY_UPDATE = 0;

        @ConfigEntry.Gui.Tooltip
        public int LEVEL_UPDATE = 0;

        @ConfigEntry.Gui.Tooltip
        public int PLAYER_DEATH_SOUND = 0;
    }
}
