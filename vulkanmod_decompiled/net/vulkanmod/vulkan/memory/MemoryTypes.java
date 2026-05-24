/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.vulkan.VkMemoryHeap
 *  org.lwjgl.vulkan.VkMemoryType
 */
package net.vulkanmod.vulkan.memory;

import java.nio.ByteBuffer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.memory.MemoryType;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import net.vulkanmod.vulkan.memory.buffer.StagingBuffer;
import net.vulkanmod.vulkan.util.VUtil;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkMemoryHeap;
import org.lwjgl.vulkan.VkMemoryType;

public class MemoryTypes {
    public static MemoryType GPU_MEM;
    public static MemoryType HOST_MEM;

    public static void createMemoryTypes() {
        VkMemoryHeap heap;
        VkMemoryType memoryType;
        int i;
        for (i = 0; i < DeviceManager.memoryProperties.memoryTypeCount(); ++i) {
            memoryType = DeviceManager.memoryProperties.memoryTypes(i);
            heap = DeviceManager.memoryProperties.memoryHeaps(memoryType.heapIndex());
            int propertyFlags = memoryType.propertyFlags();
            if (propertyFlags == 1) {
                GPU_MEM = new DeviceLocalMemory(memoryType, heap);
            }
            if (propertyFlags != 6) continue;
            HOST_MEM = new HostCoherentMemory(memoryType, heap);
        }
        if (GPU_MEM != null && HOST_MEM != null) {
            return;
        }
        for (i = 0; i < DeviceManager.memoryProperties.memoryTypeCount(); ++i) {
            memoryType = DeviceManager.memoryProperties.memoryTypes(i);
            heap = DeviceManager.memoryProperties.memoryHeaps(memoryType.heapIndex());
            if ((memoryType.propertyFlags() & 3) == 3) {
                GPU_MEM = new DeviceMappableMemory(memoryType, heap);
            }
            if ((memoryType.propertyFlags() & 6) == 6) {
                HOST_MEM = new HostLocalFallbackMemory(memoryType, heap);
            }
            if (GPU_MEM == null || HOST_MEM == null) continue;
            return;
        }
        GPU_MEM = HOST_MEM;
    }

    public static class DeviceLocalMemory
    extends MemoryType {
        DeviceLocalMemory(VkMemoryType vkMemoryType, VkMemoryHeap vkMemoryHeap) {
            super(MemoryType.Type.DEVICE_LOCAL, vkMemoryType, vkMemoryHeap);
        }

        @Override
        public void createBuffer(Buffer buffer, long size) {
            MemoryManager.getInstance().createBuffer(buffer, size, 3 | buffer.usage, 1);
        }

        @Override
        public void copyToBuffer(Buffer buffer, ByteBuffer src, long size, long srcOffset, long dstOffset) {
            StagingBuffer stagingBuffer = Vulkan.getStagingBuffer();
            stagingBuffer.copyBuffer((int)size, src);
            DeviceManager.getTransferQueue().copyBufferCmd(stagingBuffer.getId(), stagingBuffer.getOffset(), buffer.getId(), dstOffset, size);
        }

        @Override
        public void copyFromBuffer(Buffer buffer, long bufferSize, ByteBuffer byteBuffer) {
        }

        public long copyBuffer(Buffer src, Buffer dst) {
            if (dst.getBufferSize() < src.getBufferSize()) {
                throw new IllegalArgumentException("dst size is less than src size.");
            }
            return DeviceManager.getTransferQueue().copyBufferCmd(src.getId(), 0L, dst.getId(), 0L, src.getBufferSize());
        }

        @Override
        public boolean mappable() {
            return false;
        }
    }

    static class HostCoherentMemory
    extends MappableMemory {
        HostCoherentMemory(VkMemoryType vkMemoryType, VkMemoryHeap vkMemoryHeap) {
            super(MemoryType.Type.HOST_LOCAL, vkMemoryType, vkMemoryHeap);
        }

        @Override
        public void createBuffer(Buffer buffer, long size) {
            MemoryManager.getInstance().createBuffer(buffer, size, 3 | buffer.usage, 6);
        }
    }

    static class DeviceMappableMemory
    extends MappableMemory {
        DeviceMappableMemory(VkMemoryType vkMemoryType, VkMemoryHeap vkMemoryHeap) {
            super(MemoryType.Type.DEVICE_LOCAL, vkMemoryType, vkMemoryHeap);
        }

        @Override
        public void createBuffer(Buffer buffer, long size) {
            MemoryManager.getInstance().createBuffer(buffer, size, 3 | buffer.usage, 3);
        }
    }

    static class HostLocalFallbackMemory
    extends MappableMemory {
        HostLocalFallbackMemory(VkMemoryType vkMemoryType, VkMemoryHeap vkMemoryHeap) {
            super(MemoryType.Type.HOST_LOCAL, vkMemoryType, vkMemoryHeap);
        }

        @Override
        public void createBuffer(Buffer buffer, long size) {
            MemoryManager.getInstance().createBuffer(buffer, size, 3 | buffer.usage, 6);
        }
    }

    static abstract class MappableMemory
    extends MemoryType {
        MappableMemory(MemoryType.Type type, VkMemoryType vkMemoryType, VkMemoryHeap vkMemoryHeap) {
            super(type, vkMemoryType, vkMemoryHeap);
        }

        @Override
        public void copyToBuffer(Buffer buffer, ByteBuffer src, long size, long srcOffset, long dstOffset) {
            VUtil.memcpy(src, buffer, size, srcOffset, dstOffset);
        }

        @Override
        public void copyFromBuffer(Buffer buffer, long size, ByteBuffer byteBuffer) {
            MemoryUtil.memCopy((long)buffer.getDataPtr(), (long)MemoryUtil.memAddress((ByteBuffer)byteBuffer), (long)size);
            VUtil.memcpy(buffer, byteBuffer, size);
        }

        @Override
        public boolean mappable() {
            return true;
        }
    }
}

