/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  net.minecraft.class_155
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 */
package net.vulkanmod.render.profiling;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_155;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.vulkanmod.Initializer;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.render.chunk.WorldRenderer;
import net.vulkanmod.render.chunk.build.thread.BuilderResources;
import net.vulkanmod.render.profiling.BuildTimeProfiler;
import net.vulkanmod.render.profiling.Profiler;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.util.ColorUtil;

public class ProfilerOverlay {
    private static final long POLL_PERIOD = 100000000L;
    public static ProfilerOverlay INSTANCE;
    public static boolean shouldRender;
    private static Profiler.ProfilerResults lastResults;
    private static long lastPollTime;
    private static float frametime;
    private static String buildStats;
    private final class_310 minecraft;
    private final class_327 font;

    public ProfilerOverlay(class_310 minecraft) {
        this.minecraft = minecraft;
        this.font = minecraft.field_1772;
    }

    public static void createInstance(class_310 minecraft) {
        INSTANCE = new ProfilerOverlay(minecraft);
    }

    public static void toggle() {
        shouldRender = !shouldRender;
        Profiler.setActive(shouldRender);
    }

    public static void onKeyPress(int key) {
    }

    public void render(class_332 guiGraphics) {
        String line;
        int i;
        GuiRenderer.guiGraphics = guiGraphics;
        List<String> infoList = this.buildInfo();
        int lineHeight = 9;
        int xOffset = 2;
        int backgroundColor = ColorUtil.ARGB.pack(0.05f, 0.05f, 0.05f, 0.3f);
        int textColor = ColorUtil.ARGB.pack(1.0f, 1.0f, 1.0f, 1.0f);
        Objects.requireNonNull(this.font);
        VRenderSystem.enableBlend();
        for (i = 0; i < infoList.size(); ++i) {
            line = infoList.get(i);
            if (Strings.isNullOrEmpty((String)line)) continue;
            int textWidth = this.font.method_1727(line);
            int yPosition = 2 + 9 * i;
            GuiRenderer.fill(1, yPosition - 1, 2 + textWidth + 1, yPosition + 9 - 1, 0, backgroundColor);
        }
        VRenderSystem.disableBlend();
        for (i = 0; i < infoList.size(); ++i) {
            line = infoList.get(i);
            if (Strings.isNullOrEmpty((String)line)) continue;
            int yPosition = 2 + 9 * i;
            GuiRenderer.drawString(this.font, (class_2561)class_2561.method_43470((String)line), 2, yPosition, textColor, false);
        }
    }

    private List<String> buildInfo() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("");
        list.add("Profiler");
        list.add("Version: %s %s ".formatted(Initializer.getVersion(), class_155.method_16673().comp_4025()));
        this.updateResults();
        if (lastResults == null) {
            return list;
        }
        ObjectArrayList<Profiler.Result> partialResults = lastResults.getPartialResults();
        if (partialResults.size() < 2) {
            return list;
        }
        int fps = Math.round(1000.0f / frametime);
        list.add(String.format("FPS: %d Frametime: %.3f", fps, Float.valueOf(frametime)));
        list.add("");
        list.add(String.format("CPU fence wait time: %.3f", Float.valueOf(((Profiler.Result)partialResults.get((int)1)).value)));
        list.add("");
        for (Profiler.Result result : lastResults.getPartialResults()) {
            list.add(String.format("%s: %.3f", result.name, Float.valueOf(result.value)));
        }
        list.add("");
        list.add(MemoryManager.getInstance().getHeapStats());
        list.add("");
        list.add("");
        list.add(String.format("Build time: %.0fms", Float.valueOf(BuildTimeProfiler.getDeltaTime())));
        list.add(buildStats);
        return list;
    }

    private void updateResults() {
        if (System.nanoTime() - lastPollTime < 100000000L && lastResults != null) {
            return;
        }
        Profiler.ProfilerResults results = Profiler.getMainProfiler().getProfilerResults();
        if (results == null) {
            return;
        }
        frametime = results.getResult().value;
        lastResults = results;
        lastPollTime = System.nanoTime();
        buildStats = this.getBuildStats();
    }

    private String getBuildStats() {
        BuilderResources[] resourcesArray = WorldRenderer.getInstance().getTaskDispatcher().getResourcesArray();
        int totalTime = 0;
        int buildCount = 0;
        for (BuilderResources resources : resourcesArray) {
            totalTime += resources.getTotalBuildTime();
            buildCount += resources.getBuildCount();
        }
        return String.format("Builders time: %dms avg %dms (%d builds)", totalTime, totalTime / resourcesArray.length, buildCount);
    }
}

