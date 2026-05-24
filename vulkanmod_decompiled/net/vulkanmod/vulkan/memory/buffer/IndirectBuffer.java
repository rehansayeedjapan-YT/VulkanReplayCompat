/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.vulkan.memory.buffer;

import java.nio.ByteBuffer;
import net.vulkanmod.vulkan.Synchronization;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.memory.MemoryType;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import net.vulkanmod.vulkan.memory.buffer.StagingBuffer;
import net.vulkanmod.vulkan.queue.CommandPool;
import net.vulkanmod.vulkan.queue.TransferQueue;

public class IndirectBuffer
extends Buffer {
    CommandPool.CommandBuffer commandBuffer;

    public IndirectBuffer(int size, MemoryType type) {
        super("Indirect buffer", 256, type);
        this.createBuffer(size);
    }

    public void recordCopyCmd(ByteBuffer byteBuffer) {
        int size = byteBuffer.remaining();
        if ((long)size > this.bufferSize - this.usedBytes) {
            this.resizeBuffer((long)((float)this.bufferSize * 1.5f));
            this.usedBytes = 0L;
        }
        if (this.type.mappable()) {
            this.type.copyToBuffer(this, byteBuffer, size, 0L, this.usedBytes);
        } else {
            if (this.commandBuffer == null) {
                this.commandBuffer = DeviceManager.getTransferQueue().beginCommands();
            }
            StagingBuffer stagingBuffer = Vulkan.getStagingBuffer();
            stagingBuffer.copyBuffer(size, byteBuffer);
            TransferQueue.uploadBufferCmd(this.commandBuffer.getHandle(), stagingBuffer.id, stagingBuffer.offset, this.getId(), this.getUsedBytes(), size);
        }
        this.offset = this.usedBytes;
        this.usedBytes += (long)size;
    }

    public void submitUploads() {
        if (this.commandBuffer == null) {
            return;
        }
        DeviceManager.getTransferQueue().submitCommands(this.commandBuffer);
        Synchronization.INSTANCE.addCommandBuffer(this.commandBuffer);
        this.commandBuffer = null;
    }
}

