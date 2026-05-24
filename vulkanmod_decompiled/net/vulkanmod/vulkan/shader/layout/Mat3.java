/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryUtil
 */
package net.vulkanmod.vulkan.shader.layout;

import net.vulkanmod.vulkan.shader.layout.Uniform;
import net.vulkanmod.vulkan.util.MappedBuffer;
import org.lwjgl.system.MemoryUtil;

public class Mat3
extends Uniform {
    Mat3(Uniform.Info info) {
        super(info);
    }

    @Override
    void update(long ptr) {
        MappedBuffer src = (MappedBuffer)this.values.get();
        MemoryUtil.memCopy((long)(src.ptr + 0L), (long)(ptr + this.offset + 0L), (long)12L);
        MemoryUtil.memCopy((long)(src.ptr + 12L), (long)(ptr + this.offset + 16L), (long)12L);
        MemoryUtil.memCopy((long)(src.ptr + 24L), (long)(ptr + this.offset + 32L), (long)12L);
    }
}

