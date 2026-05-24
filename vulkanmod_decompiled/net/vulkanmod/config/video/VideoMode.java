/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.NotNull
 */
package net.vulkanmod.config.video;

import org.jetbrains.annotations.NotNull;

public record VideoMode(int width, int height, int bitDepth, int refreshRate) {
    @Override
    @NotNull
    public String toString() {
        return this.width + "\u00d7" + this.height + (String)(this.refreshRate > 0 ? " @ " + this.refreshRate + "Hz" : "");
    }

    public VideoMode withRefreshRate(int newRate) {
        return new VideoMode(this.width, this.height, this.bitDepth, newRate);
    }
}

