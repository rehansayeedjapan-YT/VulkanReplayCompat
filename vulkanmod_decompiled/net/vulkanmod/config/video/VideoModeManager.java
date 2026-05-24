/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  net.minecraft.class_1041
 *  net.minecraft.class_313
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.glfw.GLFWVidMode
 *  org.lwjgl.glfw.GLFWVidMode$Buffer
 */
package net.vulkanmod.config.video;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.util.ArrayList;
import net.minecraft.class_1041;
import net.minecraft.class_313;
import net.vulkanmod.Initializer;
import net.vulkanmod.config.video.VideoModeSet;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

public abstract class VideoModeManager {
    public static Long2ObjectMap<class_313> monitors;
    public static Long2ObjectMap<VideoModeSet[]> monitorToVideoModeSets;
    public static Long2ObjectMap<VideoModeSet.VideoMode> osVideoModes;
    public static long selectedMonitor;
    public static VideoModeSet.VideoMode selectedVideoMode;

    public static void init(Long2ObjectMap<class_313> monitors) {
        VideoModeManager.monitors = monitors;
        monitorToVideoModeSets.clear();
        LongIterator longIterator = VideoModeManager.monitors.keySet().iterator();
        while (longIterator.hasNext()) {
            long monitor = (Long)longIterator.next();
            VideoModeManager.addMonitorVideoModes(monitor);
        }
    }

    public static void addMonitorVideoModes(long monitor) {
        monitorToVideoModeSets.put(monitor, (Object)VideoModeManager.getVideoResolutions(monitor));
        osVideoModes.put(monitor, (Object)VideoModeManager.getCurrentVideoMode(monitor));
    }

    public static void removeMonitor(long monitor) {
        monitorToVideoModeSets.remove(monitor);
        osVideoModes.remove(monitor);
    }

    public static void applySelectedVideoMode() {
        Initializer.CONFIG.videoMode = selectedVideoMode;
    }

    public static VideoModeSet[] getVideoResolutions() {
        return (VideoModeSet[])monitorToVideoModeSets.get(selectedMonitor);
    }

    public static VideoModeSet getFirstAvailable() {
        VideoModeSet[] videoModeSets = (VideoModeSet[])monitorToVideoModeSets.get(GLFW.glfwGetPrimaryMonitor());
        if (videoModeSets != null) {
            return videoModeSets[videoModeSets.length - 1];
        }
        return VideoModeSet.getDummy();
    }

    public static VideoModeSet.VideoMode getOsVideoMode() {
        return (VideoModeSet.VideoMode)osVideoModes.get(selectedMonitor);
    }

    public static VideoModeSet.VideoMode getCurrentVideoMode(long monitor) {
        GLFWVidMode vidMode = GLFW.glfwGetVideoMode((long)monitor);
        if (vidMode == null) {
            throw new NullPointerException("Unable to get current video mode");
        }
        return new VideoModeSet.VideoMode(vidMode.width(), vidMode.height(), vidMode.redBits(), vidMode.refreshRate());
    }

    public static VideoModeSet[] getVideoResolutions(long monitor) {
        GLFWVidMode.Buffer buffer = GLFW.glfwGetVideoModes((long)monitor);
        ArrayList<VideoModeSet> videoModeSets = new ArrayList<VideoModeSet>();
        int currWidth = 0;
        int currHeight = 0;
        int currBitDepth = 0;
        VideoModeSet videoModeSet = null;
        for (int i = 0; i < buffer.limit(); ++i) {
            buffer.position(i);
            int bitDepth = buffer.redBits();
            if (buffer.redBits() < 8 || buffer.greenBits() != bitDepth || buffer.blueBits() != bitDepth) continue;
            int width = buffer.width();
            int height = buffer.height();
            int refreshRate = buffer.refreshRate();
            if (currWidth != width || currHeight != height || currBitDepth != bitDepth) {
                currWidth = width;
                currHeight = height;
                currBitDepth = bitDepth;
                videoModeSet = new VideoModeSet(currWidth, currHeight, currBitDepth);
                videoModeSets.add(videoModeSet);
            }
            videoModeSet.addRefreshRate(refreshRate);
        }
        VideoModeSet[] arr = new VideoModeSet[videoModeSets.size()];
        videoModeSets.toArray(arr);
        return arr;
    }

    public static VideoModeSet getVideoModeSet(VideoModeSet.VideoMode videoMode) {
        VideoModeSet[] videoModeSets;
        for (VideoModeSet set : videoModeSets = (VideoModeSet[])monitorToVideoModeSets.get(selectedMonitor)) {
            if (set.width != videoMode.width || set.height != videoMode.height) continue;
            return set;
        }
        return null;
    }

    public static void selectBestMonitor(class_1041 window) {
        selectedMonitor = VideoModeManager.findBestMonitor(window).method_1622();
        if (selectedMonitor == 0L) {
            selectedMonitor = GLFW.glfwGetPrimaryMonitor();
        }
    }

    public static class_313 findBestMonitor(class_1041 window) {
        long windowMonitor = GLFW.glfwGetWindowMonitor((long)window.method_4490());
        if (windowMonitor != 0L) {
            return (class_313)monitors.get(windowMonitor);
        }
        int winMinX = window.method_4499();
        int winMaxX = winMinX + window.method_4480();
        int winMinY = window.method_4477();
        int winMaxY = winMinY + window.method_4507();
        int maxArea = -1;
        class_313 result = null;
        long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
        Initializer.LOGGER.debug("Selecting monitor - primary: {}, current monitors: {}", (Object)primaryMonitor, monitors);
        for (class_313 monitor : monitors.values()) {
            int sy;
            int monMinX = monitor.method_1616();
            int monMaxX = monMinX + monitor.method_1617().method_1668();
            int monMinY = monitor.method_1618();
            int monMaxY = monMinY + monitor.method_1617().method_1669();
            int minX = Math.clamp((long)winMinX, monMinX, monMaxX);
            int maxX = Math.clamp((long)winMaxX, monMinX, monMaxX);
            int minY = Math.clamp((long)winMinY, monMinY, monMaxY);
            int maxY = Math.clamp((long)winMaxY, monMinY, monMaxY);
            int sx = Math.max(0, maxX - minX);
            int area = sx * (sy = Math.max(0, maxY - minY));
            if (area > maxArea) {
                result = monitor;
                maxArea = area;
                continue;
            }
            if (area != maxArea || primaryMonitor != monitor.method_1622()) continue;
            Initializer.LOGGER.debug("Primary monitor {} is preferred to monitor {}", (Object)monitor, (Object)result);
            result = monitor;
        }
        Initializer.LOGGER.debug("Selected monitor: {}", result);
        return result;
    }

    public static Long2ObjectMap<class_313> getMonitors() {
        return monitors;
    }

    static {
        monitorToVideoModeSets = new Long2ObjectOpenHashMap();
        osVideoModes = new Long2ObjectOpenHashMap();
    }
}

