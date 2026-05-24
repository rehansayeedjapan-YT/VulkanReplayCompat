/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryUtil
 */
package net.vulkanmod.vulkan.util;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import net.vulkanmod.vulkan.util.VUtil;
import org.lwjgl.system.MemoryUtil;

public class MappedBuffer {
    public final ByteBuffer buffer;
    public final long ptr;

    public static MappedBuffer createFromBuffer(ByteBuffer buffer) {
        return new MappedBuffer(buffer, MemoryUtil.memAddress0((Buffer)buffer));
    }

    MappedBuffer(ByteBuffer buffer, long ptr) {
        this.buffer = buffer;
        this.ptr = ptr;
    }

    public MappedBuffer(int size) {
        this.buffer = MemoryUtil.memAlloc((int)size);
        this.ptr = MemoryUtil.memAddress0((Buffer)this.buffer);
    }

    public void putFloat(int idx, float f) {
        VUtil.UNSAFE.putFloat(this.ptr + (long)idx, f);
    }

    public void putInt(int idx, int f) {
        VUtil.UNSAFE.putInt(this.ptr + (long)idx, f);
    }

    public float getFloat(int idx) {
        return VUtil.UNSAFE.getFloat(this.ptr + (long)idx);
    }

    public int getInt(int idx) {
        return VUtil.UNSAFE.getInt(this.ptr + (long)idx);
    }
}

