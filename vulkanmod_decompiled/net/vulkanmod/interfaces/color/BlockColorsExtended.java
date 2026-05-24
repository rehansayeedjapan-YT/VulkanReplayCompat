/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_324
 */
package net.vulkanmod.interfaces.color;

import net.minecraft.class_324;
import net.vulkanmod.render.chunk.build.color.BlockColorRegistry;

public interface BlockColorsExtended {
    public static BlockColorsExtended from(class_324 blockColors) {
        return (BlockColorsExtended)blockColors;
    }

    public BlockColorRegistry getColorResolverMap();
}

