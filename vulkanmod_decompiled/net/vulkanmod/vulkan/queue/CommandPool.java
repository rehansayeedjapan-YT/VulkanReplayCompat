/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.Pointer
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VkCommandBuffer
 *  org.lwjgl.vulkan.VkCommandBufferAllocateInfo
 *  org.lwjgl.vulkan.VkCommandBufferBeginInfo
 *  org.lwjgl.vulkan.VkCommandPoolCreateInfo
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkFenceCreateInfo
 *  org.lwjgl.vulkan.VkQueue
 *  org.lwjgl.vulkan.VkSemaphoreCreateInfo
 *  org.lwjgl.vulkan.VkSubmitInfo
 */
package net.vulkanmod.vulkan.queue;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.LongBuffer;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import net.vulkanmod.vulkan.Vulkan;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;

public class CommandPool {
    long id;
    private final List<CommandBuffer> commandBuffers = new ObjectArrayList();
    private final Queue<CommandBuffer> availableCmdBuffers = new ArrayDeque<CommandBuffer>();

    CommandPool(int queueFamilyIndex) {
        this.createCommandPool(queueFamilyIndex);
    }

    public void createCommandPool(int queueFamily) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc((MemoryStack)stack);
            poolInfo.sType(39);
            poolInfo.queueFamilyIndex(queueFamily);
            poolInfo.flags(2);
            LongBuffer pCommandPool = stack.mallocLong(1);
            if (VK10.vkCreateCommandPool((VkDevice)Vulkan.getVkDevice(), (VkCommandPoolCreateInfo)poolInfo, null, (LongBuffer)pCommandPool) != 0) {
                throw new RuntimeException("Failed to create command pool");
            }
            this.id = pCommandPool.get(0);
        }
    }

    public CommandBuffer getCommandBuffer() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            CommandBuffer commandBuffer = this.getCommandBuffer(stack);
            return commandBuffer;
        }
    }

    public CommandBuffer getCommandBuffer(MemoryStack stack) {
        if (this.availableCmdBuffers.isEmpty()) {
            this.allocateCommandBuffers(stack);
        }
        CommandBuffer commandBuffer = this.availableCmdBuffers.poll();
        return commandBuffer;
    }

    private void allocateCommandBuffers(MemoryStack stack) {
        int size = 10;
        VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc((MemoryStack)stack);
        allocInfo.sType$Default();
        allocInfo.level(0);
        allocInfo.commandPool(this.id);
        allocInfo.commandBufferCount(10);
        PointerBuffer pCommandBuffer = stack.mallocPointer(10);
        VK10.vkAllocateCommandBuffers((VkDevice)Vulkan.getVkDevice(), (VkCommandBufferAllocateInfo)allocInfo, (PointerBuffer)pCommandBuffer);
        VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc((MemoryStack)stack);
        fenceInfo.sType$Default();
        fenceInfo.flags(1);
        VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc((MemoryStack)stack);
        semaphoreCreateInfo.sType$Default();
        for (int i = 0; i < 10; ++i) {
            LongBuffer pFence = stack.mallocLong(1);
            VK10.vkCreateFence((VkDevice)Vulkan.getVkDevice(), (VkFenceCreateInfo)fenceInfo, null, (LongBuffer)pFence);
            LongBuffer pSemaphore = stack.mallocLong(1);
            VK10.vkCreateSemaphore((VkDevice)Vulkan.getVkDevice(), (VkSemaphoreCreateInfo)semaphoreCreateInfo, null, (LongBuffer)pSemaphore);
            VkCommandBuffer vkCommandBuffer = new VkCommandBuffer(pCommandBuffer.get(i), Vulkan.getVkDevice());
            CommandBuffer commandBuffer = new CommandBuffer(this, vkCommandBuffer, pFence.get(0), pSemaphore.get(0));
            this.commandBuffers.add(commandBuffer);
            this.availableCmdBuffers.add(commandBuffer);
        }
    }

    public void addToAvailable(CommandBuffer commandBuffer) {
        this.availableCmdBuffers.add(commandBuffer);
    }

    public void cleanUp() {
        for (CommandBuffer commandBuffer : this.commandBuffers) {
            VK10.vkDestroyFence((VkDevice)Vulkan.getVkDevice(), (long)commandBuffer.fence, null);
            VK10.vkDestroySemaphore((VkDevice)Vulkan.getVkDevice(), (long)commandBuffer.semaphore, null);
        }
        VK10.vkResetCommandPool((VkDevice)Vulkan.getVkDevice(), (long)this.id, (int)1);
        VK10.vkDestroyCommandPool((VkDevice)Vulkan.getVkDevice(), (long)this.id, null);
    }

    public long getId() {
        return this.id;
    }

    public static class CommandBuffer {
        public final CommandPool commandPool;
        public final VkCommandBuffer handle;
        public final long fence;
        public final long semaphore;
        boolean submitted;
        boolean recording;

        public CommandBuffer(CommandPool commandPool, VkCommandBuffer handle, long fence, long semaphore) {
            this.commandPool = commandPool;
            this.handle = handle;
            this.fence = fence;
            this.semaphore = semaphore;
        }

        public VkCommandBuffer getHandle() {
            return this.handle;
        }

        public long getFence() {
            return this.fence;
        }

        public long getSemaphore() {
            return this.semaphore;
        }

        public boolean isSubmitted() {
            return this.submitted;
        }

        public boolean isRecording() {
            return this.recording;
        }

        public void begin(MemoryStack stack) {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc((MemoryStack)stack);
            beginInfo.sType(42);
            beginInfo.flags(1);
            VK10.vkBeginCommandBuffer((VkCommandBuffer)this.handle, (VkCommandBufferBeginInfo)beginInfo);
            this.recording = true;
        }

        public long submitCommands(MemoryStack stack, VkQueue queue, boolean useSemaphore) {
            long fence = this.fence;
            VK10.vkEndCommandBuffer((VkCommandBuffer)this.handle);
            VK10.vkResetFences((VkDevice)Vulkan.getVkDevice(), (long)this.fence);
            VkSubmitInfo submitInfo = VkSubmitInfo.calloc((MemoryStack)stack);
            submitInfo.sType(4);
            submitInfo.pCommandBuffers(stack.pointers((Pointer)this.handle));
            if (useSemaphore) {
                submitInfo.pSignalSemaphores(stack.longs(this.semaphore));
            }
            VK10.vkQueueSubmit((VkQueue)queue, (VkSubmitInfo)submitInfo, (long)fence);
            this.recording = false;
            this.submitted = true;
            return fence;
        }

        public void reset() {
            this.submitted = false;
            this.recording = false;
            this.commandPool.addToAvailable(this);
        }
    }
}

