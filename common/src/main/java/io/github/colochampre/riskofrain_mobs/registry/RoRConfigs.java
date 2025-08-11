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
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public LemuriansConfig LEMURIANS = new LemuriansConfig();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public StoneGolemsConfig STONE_GOLEMS = new StoneGolemsConfig();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = false)
        public WispsConfig WISPS = new WispsConfig();
    }

    public static class DronesConfig implements ConfigData {
        public double GUNNER_DRONE_MAX_HEALTH = 20;
        public double GUNNER_TURRET_MAX_HEALTH = 26;
        public double BULLETS_DAMAGE = 2.0;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int DRONES_SPAWN_RATE = 1;
    }

    public static class BeetlesConfig implements ConfigData {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 999)
        public double BEETLE_MAX_HEALTH = 20.0;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 999)
        public double BEETLE_ATTACK_DAMAGE = 2.5;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int BEETLE_OVERWORLD_SPAWN_RATE = 66;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int BEETLE_NETHER_SPAWN_RATE = 10;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
        public int BEETLE_MIN_GROUP_SIZE = 1;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
        public int BEETLE_MAX_GROUP_SIZE = 3;
        public boolean BEETLE_DESPAWN = true;
    }

    public static class LemuriansConfig implements ConfigData {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 999)
        public double LEMURIAN_MAX_HEALTH = 20.0;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 999)
        public double LEMURIAN_ATTACK_DAMAGE = 2.5;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int LEMURIAN_OVERWORLD_SPAWN_RATE = 66;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int LEMURIAN_NETHER_SPAWN_RATE = 10;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
        public int LEMURIAN_MIN_GROUP_SIZE = 1;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
        public int LEMURIAN_MAX_GROUP_SIZE = 3;
        public boolean LEMURIAN_DESPAWN = true;
        public boolean ENABLE_FIREBALL_ATTACK = true;
        public boolean ENABLE_FIREBALL_GRIEF = true;
    }

    public static class StoneGolemsConfig implements ConfigData {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 999)
        public double STONE_GOLEM_MAX_HEALTH = 100.0;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 999)
        public double STONE_GOLEM_ATTACK_DAMAGE = 24.0;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int STONE_GOLEM_OVERWORLD_SPAWN_RATE = 10;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int STONE_GOLEM_NETHER_SPAWN_RATE = 2;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
        public int STONE_GOLEM_MIN_GROUP_SIZE = 1;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
        public int STONE_GOLEM_MAX_GROUP_SIZE = 1;
        public boolean STONE_GOLEM_DESPAWN = false;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int STONE_GOLEM_SPAWN_VOLUME = 100;
    }

    public static class WispsConfig implements ConfigData {
        @ConfigEntry.BoundedDiscrete(min = 1, max = 999)
        public double WISP_MAX_HEALTH = 12.0;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 999)
        public double WISP_ATTACK_DAMAGE = 2.0;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int WISP_OVERWORLD_SPAWN_RATE = 50;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int WISP_NETHER_SPAWN_RATE = 10;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
        public int WISP_MIN_GROUP_SIZE = 1;
        @ConfigEntry.BoundedDiscrete(min = 1, max = 8)
        public int WISP_MAX_GROUP_SIZE = 3;
        public boolean WISP_DESPAWN = true;
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
