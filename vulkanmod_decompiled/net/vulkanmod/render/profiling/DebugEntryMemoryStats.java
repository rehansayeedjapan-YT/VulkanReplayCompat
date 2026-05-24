/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11630
 *  net.minecraft.class_11632
 *  net.minecraft.class_1937
 *  net.minecraft.class_2818
 *  net.minecraft.class_2960
 *  org.jetbrains.annotations.Nullable
 */
package net.vulkanmod.render.profiling;

import java.util.List;
import net.minecraft.class_11630;
import net.minecraft.class_11632;
import net.minecraft.class_1937;
import net.minecraft.class_2818;
import net.minecraft.class_2960;
import net.vulkanmod.render.chunk.ChunkAreaManager;
import net.vulkanmod.render.chunk.WorldRenderer;
import org.jetbrains.annotations.Nullable;

public class DebugEntryMemoryStats
implements class_11632 {
    private static final class_2960 GROUP = class_2960.method_60656((String)"vk_memory");

    public void method_72751(class_11630 debugScreenDisplayer, @Nullable class_1937 level, @Nullable class_2818 levelChunk, @Nullable class_2818 levelChunk2) {
        ChunkAreaManager chunkAreaManager = WorldRenderer.getInstance().getChunkAreaManager();
        if (chunkAreaManager != null) {
            debugScreenDisplayer.method_72744(GROUP, List.of(chunkAreaManager.getStats()));
        }
    }
}

