/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.vulkan.memory.buffer;

import net.vulkanmod.vulkan.memory.MemoryType;
import net.vulkanmod.vulkan.memory.MemoryTypes;
import net.vulkanmod.vulkan.memory.buffer.Buffer;

public class VertexBuffer
extends Buffer {
    public VertexBuffer(int size) {
        this(size, MemoryTypes.HOST_MEM);
    }

    public VertexBuffer(int size, MemoryType type) {
        super("Vertex buffer", 128, type);
        this.createBuffer(size);
    }
}

