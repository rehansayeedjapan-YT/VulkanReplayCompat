/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_12393
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_315
 *  net.minecraft.class_4061
 *  net.minecraft.class_4063
 *  net.minecraft.class_4066
 *  net.minecraft.class_5365
 *  net.minecraft.class_6597
 *  net.minecraft.class_9927
 */
package net.vulkanmod.config.option;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.class_1041;
import net.minecraft.class_12393;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_4061;
import net.minecraft.class_4063;
import net.minecraft.class_4066;
import net.minecraft.class_5365;
import net.minecraft.class_6597;
import net.minecraft.class_9927;
import net.vulkanmod.Initializer;
import net.vulkanmod.config.Config;
import net.vulkanmod.config.gui.OptionBlock;
import net.vulkanmod.config.option.CyclingOption;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.option.OptionPage;
import net.vulkanmod.config.option.PerformanceImpact;
import net.vulkanmod.config.option.RangeOption;
import net.vulkanmod.config.option.SwitchOption;
import net.vulkanmod.config.video.VideoModeManager;
import net.vulkanmod.config.video.VideoModeSet;
import net.vulkanmod.config.video.WindowMode;
import net.vulkanmod.render.chunk.WorldRenderer;
import net.vulkanmod.render.vertex.TerrainRenderType;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.device.DeviceManager;

public abstract class Options {
    public static boolean fullscreenDirty = false;
    private static final Config config = Initializer.CONFIG;
    private static final class_310 minecraft = class_310.method_1551();
    private static final class_1041 window = minecraft.method_22683();
    private static final class_315 mcOptions = Options.minecraft.field_1690;

    public static List<OptionPage> getOptionPages() {
        ArrayList<OptionPage> optionPages = new ArrayList<OptionPage>();
        OptionPage page = new OptionPage(class_2561.method_43471((String)"vulkanmod.options.pages.video").getString(), Options.getVideoOpts());
        optionPages.add(page);
        page = new OptionPage(class_2561.method_43471((String)"vulkanmod.options.pages.graphics").getString(), Options.getGraphicsOpts());
        optionPages.add(page);
        page = new OptionPage(class_2561.method_43471((String)"vulkanmod.options.pages.optimizations").getString(), Options.getOptimizationOpts());
        optionPages.add(page);
        page = new OptionPage(class_2561.method_43471((String)"vulkanmod.options.pages.other").getString(), Options.getOtherOpts());
        optionPages.add(page);
        return optionPages;
    }

    public static OptionBlock[] getVideoOpts() {
        VideoModeManager.selectBestMonitor(window);
        VideoModeSet[] resolutions = VideoModeManager.getVideoResolutions();
        VideoModeSet.VideoMode videoMode = Options.config.videoMode;
        VideoModeSet videoModeSet = VideoModeManager.getVideoModeSet(videoMode);
        if (videoModeSet == null) {
            videoModeSet = resolutions[resolutions.length - 1];
            videoMode = videoModeSet.getVideoMode();
        }
        VideoModeManager.selectedVideoMode = videoMode;
        List<Integer> refreshRates = videoModeSet.getRefreshRates();
        Option<WindowMode> windowModeOption = new CyclingOption<WindowMode>((class_2561)class_2561.method_43471((String)"vulkanmod.options.windowMode"), WindowMode.values(), value -> {
            boolean exclusiveFullscreen = value == WindowMode.EXCLUSIVE_FULLSCREEN;
            mcOptions.method_42447().method_41748((Object)exclusiveFullscreen);
            Options.config.windowMode = value.mode;
            fullscreenDirty = true;
        }, () -> WindowMode.fromValue(Options.config.windowMode)).setTranslator(value -> class_2561.method_43471((String)WindowMode.getComponentName(value)));
        CyclingOption refreshRateOption = (CyclingOption)new CyclingOption<Integer>((class_2561)class_2561.method_43471((String)"vulkanmod.options.refreshRate"), refreshRates.toArray(new Integer[0]), value -> {
            VideoModeManager.selectedVideoMode.refreshRate = value;
            VideoModeManager.applySelectedVideoMode();
            if (((Boolean)mcOptions.method_42447().method_41753()).booleanValue()) {
                fullscreenDirty = true;
            }
        }, () -> VideoModeManager.selectedVideoMode.refreshRate).setTranslator(refreshRate -> class_2561.method_30163((String)refreshRate.toString())).setActivationFn(() -> windowModeOption.getNewValue() == WindowMode.EXCLUSIVE_FULLSCREEN);
        Option<VideoModeSet> resolutionOption = new CyclingOption<VideoModeSet>((class_2561)class_2561.method_43471((String)"options.fullscreen.resolution"), resolutions, value -> {
            VideoModeManager.selectedVideoMode = value.getVideoMode((Integer)refreshRateOption.getNewValue());
            VideoModeManager.applySelectedVideoMode();
            if (((Boolean)mcOptions.method_42447().method_41753()).booleanValue()) {
                fullscreenDirty = true;
            }
        }, () -> {
            VideoModeSet.VideoMode selectedVideoMode = VideoModeManager.selectedVideoMode;
            VideoModeSet selectedVideoModeSet = VideoModeManager.getVideoModeSet(selectedVideoMode);
            return selectedVideoModeSet != null ? selectedVideoModeSet : VideoModeSet.getDummy();
        }).setTranslator(resolution -> class_2561.method_30163((String)resolution.toString())).setActivationFn(() -> windowModeOption.getNewValue() == WindowMode.EXCLUSIVE_FULLSCREEN);
        resolutionOption.setOnChange(() -> {
            VideoModeSet newSet = (VideoModeSet)resolutionOption.getNewValue();
            Integer[] rates = newSet.getRefreshRates().toArray(new Integer[0]);
            refreshRateOption.setValues(rates);
            refreshRateOption.setNewValue(rates[rates.length - 1]);
        });
        windowModeOption.setOnChange(() -> {
            resolutionOption.updateActiveState();
            refreshRateOption.updateActiveState();
        });
        return new OptionBlock[]{new OptionBlock("", new Option[]{windowModeOption, resolutionOption, refreshRateOption, new RangeOption((class_2561)class_2561.method_43471((String)"options.framerateLimit"), 10, 260, 10, value -> class_2561.method_30163((String)(value == 260 ? class_2561.method_43471((String)"options.framerateLimit.max").getString() : String.valueOf(value))), value -> {
            mcOptions.method_42524().method_41748(value);
            minecraft.method_61964().method_61938(value.intValue());
        }, () -> (Integer)mcOptions.method_42524().method_41753()), new SwitchOption((class_2561)class_2561.method_43471((String)"options.vsync"), value -> {
            mcOptions.method_42433().method_41748(value);
            window.method_4497(value.booleanValue());
        }, () -> (Boolean)mcOptions.method_42433().method_41753()), new CyclingOption<class_9927>((class_2561)class_2561.method_43471((String)"options.inactivityFpsLimit"), class_9927.values(), value -> mcOptions.method_61970().method_41748(value), () -> (class_9927)mcOptions.method_61970().method_41753()).setTranslator(class_9927::method_76526)}), new OptionBlock("", new Option[]{new RangeOption((class_2561)class_2561.method_43471((String)"options.guiScale"), 0, window.method_4476(0, minecraft.method_1573()), 1, value -> class_2561.method_43471((String)(value == 0 ? "options.guiScale.auto" : String.valueOf(value))), value -> {
            mcOptions.method_42474().method_41748(value);
            minecraft.method_15993();
        }, () -> (Integer)mcOptions.method_42474().method_41753()), new RangeOption((class_2561)class_2561.method_43471((String)"options.gamma"), 0, 100, 1, value -> class_2561.method_43471((String)(switch (value) {
            case 0 -> "options.gamma.min";
            case 50 -> "options.gamma.default";
            case 100 -> "options.gamma.max";
            default -> String.valueOf(value);
        })), value -> mcOptions.method_42473().method_41748((Object)((double)value.intValue() * 0.01)), () -> (int)((Double)mcOptions.method_42473().method_41753() * 100.0))}), new OptionBlock("", new Option[]{new CyclingOption<class_4061>((class_2561)class_2561.method_43471((String)"options.attackIndicator"), class_4061.values(), value -> mcOptions.method_42565().method_41748(value), () -> (class_4061)mcOptions.method_42565().method_41753()).setTranslator(class_4061::method_76522), new SwitchOption((class_2561)class_2561.method_43471((String)"options.autosaveIndicator"), value -> mcOptions.method_42452().method_41748(value), () -> (Boolean)mcOptions.method_42452().method_41753())})};
    }

    public static OptionBlock[] getGraphicsOpts() {
        Option<class_12393> texFilteringOption = new CyclingOption<class_12393>((class_2561)class_2561.method_43471((String)"options.textureFiltering"), class_12393.values(), value -> {
            class_12393 oldValue = (class_12393)mcOptions.method_76747().method_41753();
            if (oldValue == class_12393.field_64665 && value != class_12393.field_64665 || value == class_12393.field_64665 && oldValue != class_12393.field_64665) {
                minecraft.method_1513();
                WorldRenderer.getInstance().resetSampler();
            }
            mcOptions.method_76747().method_41748(value);
        }, () -> (class_12393)mcOptions.method_76747().method_41753()).setTranslator(class_12393::method_76751).setTooltip(value -> switch (value) {
            default -> throw new MatchException(null, null);
            case class_12393.field_64663 -> class_2561.method_43471((String)"options.textureFiltering.none.tooltip");
            case class_12393.field_64664 -> class_2561.method_43471((String)"options.textureFiltering.rgss.tooltip");
            case class_12393.field_64665 -> class_2561.method_43471((String)"options.textureFiltering.anisotropic.tooltip");
        }).setImpact(PerformanceImpact.MEDIUM);
        Option<Integer> maxAnisotropyOption = new RangeOption((class_2561)class_2561.method_43471((String)"options.maxAnisotropy"), 1, 3, 1, value -> {
            Integer oldValue = (Integer)mcOptions.method_76247().method_41753();
            if (mcOptions.method_76747().method_41753() == class_12393.field_64665 && !oldValue.equals(value)) {
                minecraft.method_1513();
                WorldRenderer.getInstance().resetSampler();
            }
            mcOptions.method_76247().method_41748(value);
        }, () -> (Integer)mcOptions.method_76247().method_41753()).setTranslator(value -> class_2561.method_43469((String)"options.multiplier", (Object[])new Object[]{Integer.toString(1 << value)})).setTooltip(v -> class_2561.method_43471((String)"options.maxAnisotropy.tooltip"));
        maxAnisotropyOption.setActivationFn(() -> texFilteringOption.getNewValue() == class_12393.field_64665);
        texFilteringOption.setOnChange(maxAnisotropyOption::updateActiveState);
        return new OptionBlock[]{new OptionBlock("", new Option[]{new RangeOption((class_2561)class_2561.method_43471((String)"options.renderDistance"), 2, 32, 1, value -> mcOptions.method_42503().method_41748(value), () -> (Integer)mcOptions.method_42503().method_41753()).setTooltip(v -> class_2561.method_43470((String)"Chunk render distance")).setImpact(PerformanceImpact.HIGH), new RangeOption((class_2561)class_2561.method_43471((String)"options.simulationDistance"), 5, 32, 1, value -> mcOptions.method_42510().method_41748(value), () -> (Integer)mcOptions.method_42510().method_41753()), new CyclingOption<class_6597>((class_2561)class_2561.method_43471((String)"options.prioritizeChunkUpdates"), class_6597.values(), value -> mcOptions.method_41798().method_41748(value), () -> (class_6597)mcOptions.method_41798().method_41753()).setTranslator(class_6597::method_76535)}), new OptionBlock("", new Option[]{new CyclingOption<class_5365>((class_2561)class_2561.method_43471((String)"options.graphics.preset"), new class_5365[]{class_5365.field_25427, class_5365.field_25428, class_5365.field_63461}, value -> mcOptions.method_75329().method_41748(value), () -> (class_5365)mcOptions.method_75329().method_41753()).setTranslator(g -> class_2561.method_43471((String)g.method_75305())), texFilteringOption, maxAnisotropyOption, new CyclingOption<class_4066>((class_2561)class_2561.method_43471((String)"options.particles"), new class_4066[]{class_4066.field_18199, class_4066.field_18198, class_4066.field_18197}, value -> mcOptions.method_42475().method_41748(value), () -> (class_4066)mcOptions.method_42475().method_41753()).setImpact(PerformanceImpact.MEDIUM).setTranslator(class_4066::method_76329), new CyclingOption<class_4063>((class_2561)class_2561.method_43471((String)"options.renderClouds"), class_4063.values(), value -> mcOptions.method_42528().method_41748(value), () -> (class_4063)mcOptions.method_42528().method_41753()).setTranslator(class_4063::method_76525), new RangeOption((class_2561)class_2561.method_43471((String)"options.renderCloudsDistance"), 2, 128, 1, value -> mcOptions.method_71270().method_41748(value), () -> (Integer)mcOptions.method_71270().method_41753()), new SwitchOption((class_2561)class_2561.method_43471((String)"options.cutoutLeaves"), value -> mcOptions.method_75334().method_41748(value), () -> (Boolean)mcOptions.method_75334().method_41753()).setTooltip(value -> class_2561.method_43471((String)"options.cutoutLeaves.tooltip")), new RangeOption((class_2561)class_2561.method_43471((String)"options.chunkFade"), 0, 40, 1, value -> mcOptions.method_76253().method_41748((Object)((double)value.intValue() / 20.0)), () -> (int)((Double)mcOptions.method_76253().method_41753() * 20.0)).setTranslator(value -> class_2561.method_43470((String)String.valueOf((float)value.intValue() / 20.0f))).setTooltip(v -> class_2561.method_43471((String)"options.chunkFade.tooltip")), new CyclingOption<Integer>((class_2561)class_2561.method_43471((String)"options.ao"), new Integer[]{0, 1, 2}, value -> {
            mcOptions.method_41792().method_41748((Object)(value > 0 ? 1 : 0));
            Options.config.ambientOcclusion = value;
            Options.minecraft.field_1769.method_3279();
        }, () -> Options.config.ambientOcclusion).setTranslator(value -> class_2561.method_43471((String)(switch (value) {
            case 0 -> "options.off";
            case 1 -> "options.on";
            case 2 -> "vulkanmod.options.ao.subBlock";
            default -> "vulkanmod.options.unknown";
        }))).setTooltip(value -> value == 2 ? class_2561.method_43471((String)"vulkanmod.options.ao.subBlock.tooltip") : class_2561.method_43473()).setImpact(PerformanceImpact.LOW), new RangeOption((class_2561)class_2561.method_43471((String)"options.biomeBlendRadius"), 0, 7, 1, value -> class_2561.method_30163((String)"%d x %d".formatted(value * 2 + 1, value * 2 + 1)), value -> {
            mcOptions.method_41805().method_41748(value);
            Options.minecraft.field_1769.method_3279();
        }, () -> (Integer)mcOptions.method_41805().method_41753())}), new OptionBlock("", new Option[]{new SwitchOption((class_2561)class_2561.method_43471((String)"options.entityShadows"), value -> mcOptions.method_42435().method_41748(value), () -> (Boolean)mcOptions.method_42435().method_41753()).setImpact(PerformanceImpact.LOW), new RangeOption((class_2561)class_2561.method_43471((String)"options.entityDistanceScaling"), 2, 20, 1, value -> mcOptions.method_42517().method_41748((Object)((double)value.intValue() / 4.0)), () -> (int)((Double)mcOptions.method_42517().method_41753() * 4.0)).setImpact(PerformanceImpact.HIGH).setTranslator(value -> class_2561.method_43470((String)String.valueOf((double)value.intValue() / 4.0))), new CyclingOption<Integer>((class_2561)class_2561.method_43471((String)"options.mipmapLevels"), new Integer[]{0, 1, 2, 3, 4}, value -> {
            mcOptions.method_42563().method_41748(value);
            minecraft.method_24041(value.intValue());
            minecraft.method_1513();
        }, () -> (Integer)mcOptions.method_42563().method_41753()).setTranslator(v -> class_2561.method_43470((String)String.valueOf(v))).setImpact(PerformanceImpact.LOW), new RangeOption((class_2561)class_2561.method_43471((String)"options.weatherRadius"), 3, 10, 1, value -> mcOptions.method_75333().method_41748(value), () -> (Integer)mcOptions.method_75333().method_41753()).setTooltip(value -> class_2561.method_43471((String)"options.weatherRadius.tooltip")), new SwitchOption((class_2561)class_2561.method_43471((String)"options.vignette"), value -> mcOptions.method_75335().method_41748(value), () -> (Boolean)mcOptions.method_75335().method_41753()).setTooltip(value -> class_2561.method_43471((String)"options.vignette.tooltip"))})};
    }

    public static OptionBlock[] getOptimizationOpts() {
        return new OptionBlock[]{new OptionBlock("", new Option[]{new CyclingOption<Integer>((class_2561)class_2561.method_43471((String)"vulkanmod.options.advCulling"), new Integer[]{1, 2, 3, 10}, value -> {
            Options.config.advCulling = value;
        }, () -> Options.config.advCulling).setTranslator(v -> class_2561.method_43471((String)(switch (v) {
            case 1 -> "vulkanmod.options.advCulling.aggressive";
            case 2 -> "vulkanmod.options.advCulling.normal";
            case 3 -> "vulkanmod.options.advCulling.conservative";
            case 10 -> "options.off";
            default -> "vulkanmod.options.unknown";
        }))).setTooltip(v -> v <= 3 ? class_2561.method_43471((String)"vulkanmod.options.advCulling.tooltip") : class_2561.method_43473()).setImpact(PerformanceImpact.HIGH), new SwitchOption((class_2561)class_2561.method_43471((String)"vulkanmod.options.entityCulling"), v -> {
            Options.config.entityCulling = v;
        }, () -> Options.config.entityCulling).setTooltip(v -> class_2561.method_43471((String)"vulkanmod.options.entityCulling.tooltip")).setImpact(PerformanceImpact.HIGH), new SwitchOption((class_2561)class_2561.method_43471((String)"vulkanmod.options.uniqueOpaqueLayer"), v -> {
            Options.config.uniqueOpaqueLayer = v;
            TerrainRenderType.updateMapping();
            Options.minecraft.field_1769.method_3279();
        }, () -> Options.config.uniqueOpaqueLayer).setTooltip(v -> class_2561.method_43471((String)"vulkanmod.options.uniqueOpaqueLayer.tooltip")).setImpact(PerformanceImpact.HIGH), new SwitchOption((class_2561)class_2561.method_43471((String)"vulkanmod.options.backfaceCulling"), v -> {
            Options.config.backFaceCulling = v;
            Options.minecraft.field_1769.method_3279();
        }, () -> Options.config.backFaceCulling).setTooltip(v -> class_2561.method_43471((String)"vulkanmod.options.backfaceCulling.tooltip")).setImpact(PerformanceImpact.HIGH), new SwitchOption((class_2561)class_2561.method_43471((String)"vulkanmod.options.indirectDraw"), v -> {
            Options.config.indirectDraw = v;
        }, () -> Options.config.indirectDraw).setTooltip(v -> class_2561.method_43471((String)"vulkanmod.options.indirectDraw.tooltip")).setImpact(PerformanceImpact.HIGH)})};
    }

    public static OptionBlock[] getOtherOpts() {
        return new OptionBlock[]{new OptionBlock("", new Option[]{new RangeOption((class_2561)class_2561.method_43471((String)"vulkanmod.options.builderThreads"), 0, Runtime.getRuntime().availableProcessors() - 1, 1, value -> {
            Options.config.builderThreads = value;
            WorldRenderer.getInstance().getTaskDispatcher().createThreads((int)value);
        }, () -> Options.config.builderThreads).setTranslator(v -> v == 0 ? class_2561.method_43471((String)"vulkanmod.options.builderThreads.auto") : class_2561.method_43470((String)String.valueOf(v))), new RangeOption((class_2561)class_2561.method_43471((String)"vulkanmod.options.frameQueue"), 2, 5, 1, value -> {
            Options.config.frameQueueSize = value;
            Renderer.scheduleSwapChainUpdate();
        }, () -> Options.config.frameQueueSize).setTooltip(v -> class_2561.method_43471((String)"vulkanmod.options.frameQueue.tooltip")), new SwitchOption((class_2561)class_2561.method_43471((String)"vulkanmod.options.textureAnimations"), v -> {
            Options.config.textureAnimations = v;
        }, () -> Options.config.textureAnimations)}), new OptionBlock("", new Option[]{new CyclingOption<Integer>((class_2561)class_2561.method_43471((String)"vulkanmod.options.deviceSelector"), (Integer[])IntStream.range(-1, DeviceManager.suitableDevices.size()).boxed().toArray(Integer[]::new), value -> {
            Options.config.device = value;
        }, () -> Options.config.device).setTranslator(v -> class_2561.method_43471((String)(v == -1 ? "vulkanmod.options.deviceSelector.auto" : DeviceManager.suitableDevices.get((int)v.intValue()).deviceName))).setTooltip(v -> class_2561.method_43470((String)(class_2561.method_43471((String)"vulkanmod.options.deviceSelector.tooltip").getString() + ": " + DeviceManager.device.deviceName)))})};
    }
}

