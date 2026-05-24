/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryUtil
 */
package net.vulkanmod.render.chunk.util;

import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

public class BufferUtil {
    public static ByteBuffer clone(ByteBuffer src) {
        ByteBuffer ret = MemoryUtil.memAlloc((int)src.remaining());
        MemoryUtil.memCopy((ByteBuffer)src, (ByteBuffer)ret);
        return ret;
    }

    public static ByteBuffer bufferSlice(ByteBuffer buffer, int start, int end) {
        return MemoryUtil.memSlice((ByteBuffer)buffer, (int)start, (int)(end - start));
    }
}

