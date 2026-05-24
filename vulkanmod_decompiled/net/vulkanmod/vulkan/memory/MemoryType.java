/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.vulkan.VkMemoryHeap
 *  org.lwjgl.vulkan.VkMemoryType
 */
package net.vulkanmod.vulkan.memory;

import java.nio.ByteBuffer;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import org.lwjgl.vulkan.VkMemoryHeap;
import org.lwjgl.vulkan.VkMemoryType;

public abstract class MemoryType {
    final Type type;
    public final VkMemoryType vkMemoryType;
    public final VkMemoryHeap vkMemoryHeap;

    MemoryType(Type type, VkMemoryType vkMemoryType, VkMemoryHeap vkMemoryHeap) {
        this.type = type;
        this.vkMemoryType = vkMemoryType;
        this.vkMemoryHeap = vkMemoryHeap;
    }

    public abstract void createBuffer(Buffer var1, long var2);

    public abstract void copyToBuffer(Buffer var1, ByteBuffer var2, long var3, long var5, long var7);

    public abstract void copyFromBuffer(Buffer var1, long var2, ByteBuffer var4);

    public abstract boolean mappable();

    public Type getType() {
        return this.type;
    }

    public static enum Type {
        DEVICE_LOCAL,
        HOST_LOCAL;

    }
}

