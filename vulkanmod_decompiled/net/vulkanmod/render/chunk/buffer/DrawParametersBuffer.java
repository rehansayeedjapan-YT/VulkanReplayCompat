/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryUtil
 */
package net.vulkanmod.render.chunk.buffer;

import net.vulkanmod.render.chunk.cull.QuadFacing;
import net.vulkanmod.render.vertex.TerrainRenderType;
import org.lwjgl.system.MemoryUtil;

public abstract class DrawParametersBuffer {
    static final long INDEX_COUNT_OFFSET = 0L;
    static final long FIRST_INDEX_OFFSET = 4L;
    static final long VERTEX_OFFSET_OFFSET = 8L;
    static final long BASE_INSTANCE_OFFSET = 12L;
    public static final long STRIDE = 16L;
    static final int SECTIONS = 512;
    static final int FACINGS = 7;

    public static long allocateBuffer() {
        long drawParamsPtr;
        int size = (int)((long)(512 * TerrainRenderType.VALUES.length * QuadFacing.COUNT) * 16L);
        for (long ptr = drawParamsPtr = MemoryUtil.nmemAlignedAlloc((long)32L, (long)size); ptr < drawParamsPtr + (long)size; ptr += 16L) {
            DrawParametersBuffer.resetParameters(ptr);
        }
        return drawParamsPtr;
    }

    public static void freeBuffer(long ptr) {
        MemoryUtil.nmemAlignedFree((long)ptr);
    }

    public static long getParamsPtr(long basePtr, int section, int renderType, int facing) {
        return basePtr + (long)((renderType * 512 + section) * 7 + facing) * 16L;
    }

    public static void resetParameters(long ptr) {
        DrawParametersBuffer.setIndexCount(ptr, 0);
        DrawParametersBuffer.setFirstIndex(ptr, 0);
        DrawParametersBuffer.setVertexOffset(ptr, -1);
        DrawParametersBuffer.setBaseInstance(ptr, 0);
    }

    public static void setIndexCount(long ptr, int value) {
        MemoryUtil.memPutInt((long)(ptr + 0L), (int)value);
    }

    public static void setFirstIndex(long ptr, int value) {
        MemoryUtil.memPutInt((long)(ptr + 4L), (int)value);
    }

    public static void setVertexOffset(long ptr, int value) {
        MemoryUtil.memPutInt((long)(ptr + 8L), (int)value);
    }

    public static void setBaseInstance(long ptr, int value) {
        MemoryUtil.memPutInt((long)(ptr + 12L), (int)value);
    }

    public static int getIndexCount(long ptr) {
        return MemoryUtil.memGetInt((long)(ptr + 0L));
    }

    public static int getFirstIndex(long ptr) {
        return MemoryUtil.memGetInt((long)(ptr + 4L));
    }

    public static int getVertexOffset(long ptr) {
        return MemoryUtil.memGetInt((long)(ptr + 8L));
    }

    public static int getBaseInstance(long ptr) {
        return MemoryUtil.memGetInt((long)(ptr + 12L));
    }
}

