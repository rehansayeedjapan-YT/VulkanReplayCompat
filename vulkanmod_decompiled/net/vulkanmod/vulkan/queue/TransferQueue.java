/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VkBufferCopy
 *  org.lwjgl.vulkan.VkBufferCopy$Buffer
 *  org.lwjgl.vulkan.VkCommandBuffer
 *  org.lwjgl.vulkan.VkDevice
 */
package net.vulkanmod.vulkan.queue;

import net.vulkanmod.vulkan.Synchronization;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.queue.CommandPool;
import net.vulkanmod.vulkan.queue.Queue;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;

public class TransferQueue
extends Queue {
    private static final VkDevice DEVICE = Vulkan.getVkDevice();

    public TransferQueue(MemoryStack stack, int familyIndex) {
        super(stack, familyIndex);
    }

    public long copyBufferCmd(long srcBuffer, long srcOffset, long dstBuffer, long dstOffset, long size) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            CommandPool.CommandBuffer commandBuffer = this.beginCommands();
            VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc((int)1, (MemoryStack)stack);
            copyRegion.size(size);
            copyRegion.srcOffset(srcOffset);
            copyRegion.dstOffset(dstOffset);
            VK10.vkCmdCopyBuffer((VkCommandBuffer)commandBuffer.getHandle(), (long)srcBuffer, (long)dstBuffer, (VkBufferCopy.Buffer)copyRegion);
            this.submitCommands(commandBuffer);
            Synchronization.INSTANCE.addCommandBuffer(commandBuffer);
            long l = commandBuffer.fence;
            return l;
        }
    }

    public void uploadBufferImmediate(long srcBuffer, long srcOffset, long dstBuffer, long dstOffset, long size) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            CommandPool.CommandBuffer commandBuffer = this.beginCommands();
            VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc((int)1, (MemoryStack)stack);
            copyRegion.size(size);
            copyRegion.srcOffset(srcOffset);
            copyRegion.dstOffset(dstOffset);
            VK10.vkCmdCopyBuffer((VkCommandBuffer)commandBuffer.getHandle(), (long)srcBuffer, (long)dstBuffer, (VkBufferCopy.Buffer)copyRegion);
            this.submitCommands(commandBuffer);
            VK10.vkWaitForFences((VkDevice)DEVICE, (long)commandBuffer.fence, (boolean)true, (long)-1L);
            commandBuffer.reset();
        }
    }

    public static void uploadBufferCmd(VkCommandBuffer commandBuffer, long srcBuffer, long srcOffset, long dstBuffer, long dstOffset, long size) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc((int)1, (MemoryStack)stack);
            copyRegion.size(size);
            copyRegion.srcOffset(srcOffset);
            copyRegion.dstOffset(dstOffset);
            VK10.vkCmdCopyBuffer((VkCommandBuffer)commandBuffer, (long)srcBuffer, (long)dstBuffer, (VkBufferCopy.Buffer)copyRegion);
        }
    }
}

