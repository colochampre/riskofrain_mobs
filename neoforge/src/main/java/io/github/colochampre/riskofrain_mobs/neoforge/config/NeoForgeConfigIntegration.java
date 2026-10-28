package io.github.colochampre.riskofrain_mobs.neoforge.config;

import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import me.shedaniel.autoconfig.AutoConfig;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.ConfigScreenHandler;

public class NeoForgeConfigIntegration {
    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory((client, parent) ->
                        AutoConfig.getConfigScreen(RoRConfigs.class, parent).get()));
    }
}
