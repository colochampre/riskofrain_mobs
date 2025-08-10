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

    @ConfigEntry.Gui.TransitiveObject
    public MobsConfig MOBS = new MobsConfig();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
    @ConfigEntry.Gui.Tooltip
    public SoundsConfig SOUNDS = new SoundsConfig();

    public static void init() {
        AutoConfig.register(RoRConfigs.class, GsonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(RoRConfigs.class).getConfig();
    }

    public static RoRConfigs get() {
        return INSTANCE;
    }

    public static class MobsConfig implements ConfigData {
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public DronesConfig DRONES = new DronesConfig();

        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public BeetlesConfig BEETLES = new BeetlesConfig();
    }

    public static class DronesConfig implements ConfigData {
        public double GUNNER_DRONE_MAX_HEALTH = 20;
        public double GUNNER_TURRET_MAX_HEALTH = 26;
        public double BULLETS_DAMAGE = 2.0;

        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int DRONES_SPAWN_RATE = 1;
    }

    public static class BeetlesConfig implements ConfigData {
        public double BEETLE_MAX_HEALTH = 20.0;
        public double BEETLE_ATTACK_DAMAGE = 2.5;

        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int BEETLE_OVERWORLD_SPAWN_RATE = 66;

        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int BEETLE_NETHER_SPAWN_RATE = 10;

        @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
        public int BEETLE_MIN_GROUP_SIZE = 1;

        @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
        public int BEETLE_MAX_GROUP_SIZE = 3;
    }

    public static class SoundsConfig implements ConfigData {
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int ADVANCEMENT = 0;

        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int CHAT_MESSAGE = 0;

        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int DIFFICULTY_UPDATE = 0;

        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int LEVEL_UPDATE = 0;

        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int PLAYER_DEATH_SOUND = 0;
    }
}
