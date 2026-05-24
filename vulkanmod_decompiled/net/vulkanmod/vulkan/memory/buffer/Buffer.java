/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryStack
 */
package net.vulkanmod.vulkan.memory.buffer;

import java.nio.ByteBuffer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.memory.MemoryType;
import org.lwjgl.system.MemoryStack;

public class Buffer {
    public final String name;
    public final MemoryType type;
    public final int usage;
    protected long id;
    protected long allocation;
    protected long bufferSize;
    protected long usedBytes;
    protected long offset;
    protected long dataPtr;

    public Buffer(String name, int usage, MemoryType type) {
        this.name = name;
        this.usage = usage;
        this.type = type;
    }

    public void createBuffer(long bufferSize) {
        this.type.createBuffer(this, bufferSize);
        if (this.name != null) {
            try (MemoryStack stack = MemoryStack.stackPush();){
                Vulkan.setDebugLabel(stack, 9, this.id, this.name);
            }
        }
        if (this.type.mappable()) {
            this.dataPtr = MemoryManager.getInstance().Map(this.allocation).get(0);
        }
    }

    public void resizeBuffer(long newSize) {
        MemoryManager.getInstance().addToFreeable(this);
        this.createBuffer(newSize);
    }

    public void copyBuffer(ByteBuffer byteBuffer, int size) {
        if ((long)size > this.bufferSize - this.usedBytes) {
            this.resizeBuffer((this.bufferSize + (long)size) * 2L);
        }
        this.type.copyToBuffer(this, byteBuffer, size, 0L, this.usedBytes);
        this.offset = this.usedBytes;
        this.usedBytes += (long)size;
    }

    public void copyBuffer(ByteBuffer byteBuffer, int size, int dstOffset) {
        if ((long)size > this.bufferSize - (long)dstOffset) {
            this.resizeBuffer((this.bufferSize + (long)size) * 2L);
        }
        this.type.copyToBuffer(this, byteBuffer, size, 0L, dstOffset);
        this.offset = dstOffset;
        this.usedBytes = dstOffset + size;
    }

    public void scheduleFree() {
        MemoryManager.getInstance().addToFreeable(this);
    }

    public void reset() {
        this.usedBytes = 0L;
    }

    public long getAllocation() {
        return this.allocation;
    }

    public long getUsedBytes() {
        return this.usedBytes;
    }

    public long getOffset() {
        return this.offset;
    }

    public long getId() {
        return this.id;
    }

    public long getBufferSize() {
        return this.bufferSize;
    }

    public long getDataPtr() {
        return this.dataPtr;
    }

    public void setBufferSize(long size) {
        this.bufferSize = size;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAllocation(long allocation) {
        this.allocation = allocation;
    }

    public BufferInfo getBufferInfo() {
        return new BufferInfo(this.id, this.allocation, this.bufferSize, this.type.getType());
    }

    public record BufferInfo(long id, long allocation, long bufferSize, MemoryType.Type type) {
    }
}

