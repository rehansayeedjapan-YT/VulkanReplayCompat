/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.renderer.v1.Renderer
 *  net.fabricmc.loader.api.FabricLoader
 *  net.fabricmc.loader.api.ModContainer
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package net.vulkanmod;

import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.vulkanmod.config.Config;
import net.vulkanmod.config.Platform;
import net.vulkanmod.config.UpdateChecker;
import net.vulkanmod.render.chunk.build.frapi.VulkanModRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Initializer
implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger((String)"VulkanMod");
    private static String VERSION;
    public static Config CONFIG;

    public void onInitializeClient() {
        VERSION = ((ModContainer)FabricLoader.getInstance().getModContainer("vulkanmod").get()).getMetadata().getVersion().getFriendlyString();
        LOGGER.info("== VulkanMod ==");
        Platform.init();
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("vulkanmod_settings.json");
        CONFIG = Initializer.loadConfig(configPath);
        Renderer.register((Renderer)VulkanModRenderer.INSTANCE);
        UpdateChecker.checkForUpdates();
    }

    private static Config loadConfig(Path path) {
        return Config.load(path);
    }

    public static String getVersion() {
        return VERSION;
    }
}

