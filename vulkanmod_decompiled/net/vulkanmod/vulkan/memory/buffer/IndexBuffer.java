/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.vulkan.memory.buffer;

import net.vulkanmod.vulkan.memory.MemoryType;
import net.vulkanmod.vulkan.memory.buffer.Buffer;

public class IndexBuffer
extends Buffer {
    public IndexType indexType;

    public IndexBuffer(int size, MemoryType type) {
        this(size, type, IndexType.UINT16);
    }

    public IndexBuffer(int size, MemoryType type, IndexType indexType) {
        super("Index buffer", 64, type);
        this.indexType = indexType;
        this.createBuffer(size);
    }

    public static enum IndexType {
        UINT16(2, 0),
        UINT32(4, 1);

        public final int size;
        public final int value;

        private IndexType(int size, int value) {
            this.size = size;
            this.value = value;
        }
    }
}

