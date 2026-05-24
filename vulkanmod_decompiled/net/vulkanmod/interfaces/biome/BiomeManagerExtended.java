/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4543
 */
package net.vulkanmod.interfaces.biome;

import net.minecraft.class_4543;

public interface BiomeManagerExtended {
    public static BiomeManagerExtended of(class_4543 biomeManager) {
        return (BiomeManagerExtended)biomeManager;
    }

    public long getBiomeZoomSeed();
}

