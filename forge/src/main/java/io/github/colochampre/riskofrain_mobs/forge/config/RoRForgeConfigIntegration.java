package io.github.colochampre.riskofrain_mobs.forge.config;

import io.github.colochampre.riskofrain_mobs.registry.RoRConfigs;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public class RoRForgeConfigIntegration {
    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () ->
                new ConfigScreenHandler.ConfigScreenFactory((client, parent) ->
                        AutoConfig.getConfigScreen(RoRConfigs.class, parent).get()));
    }
}
