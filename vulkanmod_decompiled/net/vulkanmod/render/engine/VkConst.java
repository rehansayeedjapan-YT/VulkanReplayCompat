/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.textures.AddressMode
 *  com.mojang.blaze3d.textures.FilterMode
 */
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;

public class VkConst {
    public static int of(AddressMode addressMode) {
        return switch (addressMode) {
            default -> throw new MatchException(null, null);
            case AddressMode.REPEAT -> 0;
            case AddressMode.CLAMP_TO_EDGE -> 2;
        };
    }

    public static int of(FilterMode filterMode) {
        return switch (filterMode) {
            default -> throw new MatchException(null, null);
            case FilterMode.NEAREST -> 0;
            case FilterMode.LINEAR -> 1;
        };
    }
}

