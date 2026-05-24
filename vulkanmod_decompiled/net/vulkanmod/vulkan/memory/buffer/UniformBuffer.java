/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.vulkan.memory.buffer;

import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.memory.MemoryType;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import net.vulkanmod.vulkan.util.VUtil;

public class UniformBuffer
extends Buffer {
    private static final int MIN_OFFSET_ALIGNMENT = (int)DeviceManager.deviceProperties.limits().minUniformBufferOffsetAlignment();

    public static int getAlignedSize(int uploadSize) {
        return VUtil.align(uploadSize, MIN_OFFSET_ALIGNMENT);
    }

    public UniformBuffer(int size, MemoryType memoryType) {
        super("Uniform buffer", 16, memoryType);
        this.createBuffer(size);
    }

    public void checkCapacity(int size) {
        if ((long)size > this.bufferSize - this.usedBytes) {
            this.resizeBuffer((this.bufferSize + (long)size) * 2L);
        }
    }

    public void updateOffset(int alignedSize) {
        this.usedBytes += (long)alignedSize;
    }

    public long getPointer() {
        return this.dataPtr + this.usedBytes;
    }
}

