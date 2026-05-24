/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.vulkanmod.config.video;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;

public class VideoModeSet {
    public final int width;
    public final int height;
    public final int bitDepth;
    List<Integer> refreshRates = new ObjectArrayList();

    public static VideoModeSet getDummy() {
        VideoModeSet set = new VideoModeSet(-1, -1, -1);
        set.addRefreshRate(-1);
        return set;
    }

    public VideoModeSet(int width, int height, int bitDepth) {
        this.width = width;
        this.height = height;
        this.bitDepth = bitDepth;
    }

    public int getRefreshRate() {
        return this.refreshRates.get(0);
    }

    public boolean hasRefreshRate(int r) {
        return this.refreshRates.contains(r);
    }

    public List<Integer> getRefreshRates() {
        return this.refreshRates;
    }

    void addRefreshRate(int rr) {
        this.refreshRates.add(rr);
    }

    public String toString() {
        return this.width + " x " + this.height;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        VideoModeSet that = (VideoModeSet)o;
        return this.width == that.width && this.height == that.height && this.bitDepth == that.bitDepth && this.refreshRates.equals(that.refreshRates);
    }

    public VideoMode getVideoMode(int refresh) {
        int idx = this.refreshRates.indexOf(refresh);
        if (idx == -1) {
            idx = 0;
        }
        return new VideoMode(this.width, this.height, this.bitDepth, this.refreshRates.get(idx));
    }

    public VideoMode getVideoMode() {
        int refreshRate = this.refreshRates.get(this.refreshRates.size() - 1);
        return new VideoMode(this.width, this.height, this.bitDepth, refreshRate);
    }

    public static final class VideoMode {
        public int width;
        public int height;
        public int bitDepth;
        public int refreshRate;

        public VideoMode(int width, int height, int bitDepth, int refreshRate) {
            this.width = width;
            this.height = height;
            this.bitDepth = bitDepth;
            this.refreshRate = refreshRate;
        }

        public String toString() {
            return "VideoMode[width=" + this.width + ", height=" + this.height + ", bitDepth=" + this.bitDepth + ", refreshRate=" + this.refreshRate + "]";
        }
    }
}

