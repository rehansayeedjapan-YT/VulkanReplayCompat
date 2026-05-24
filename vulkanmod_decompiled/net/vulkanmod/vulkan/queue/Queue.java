/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.vulkan.KHRSurface
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkPhysicalDevice
 *  org.lwjgl.vulkan.VkQueue
 *  org.lwjgl.vulkan.VkQueueFamilyProperties
 *  org.lwjgl.vulkan.VkQueueFamilyProperties$Buffer
 */
package net.vulkanmod.vulkan.queue;

import java.nio.IntBuffer;
import java.util.stream.IntStream;
import net.vulkanmod.Initializer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.queue.CommandPool;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

public abstract class Queue {
    private static VkDevice device;
    private static QueueFamilyIndices queueFamilyIndices;
    private final VkQueue vkQueue;
    protected CommandPool commandPool;

    public synchronized CommandPool.CommandBuffer beginCommands() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            CommandPool.CommandBuffer commandBuffer = this.commandPool.getCommandBuffer(stack);
            commandBuffer.begin(stack);
            CommandPool.CommandBuffer commandBuffer2 = commandBuffer;
            return commandBuffer2;
        }
    }

    Queue(MemoryStack stack, int familyIndex) {
        this(stack, familyIndex, true);
    }

    Queue(MemoryStack stack, int familyIndex, boolean initCommandPool) {
        PointerBuffer pQueue = stack.mallocPointer(1);
        VK10.vkGetDeviceQueue((VkDevice)DeviceManager.vkDevice, (int)familyIndex, (int)0, (PointerBuffer)pQueue);
        this.vkQueue = new VkQueue(pQueue.get(0), DeviceManager.vkDevice);
        if (initCommandPool) {
            this.commandPool = new CommandPool(familyIndex);
        }
    }

    public long submitCommands(CommandPool.CommandBuffer commandBuffer) {
        return this.submitCommands(commandBuffer, false);
    }

    public synchronized long submitCommands(CommandPool.CommandBuffer commandBuffer, boolean useSemaphore) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            long l = commandBuffer.submitCommands(stack, this.vkQueue, useSemaphore);
            return l;
        }
    }

    public VkQueue vkQueue() {
        return this.vkQueue;
    }

    public void cleanUp() {
        if (this.commandPool != null) {
            this.commandPool.cleanUp();
        }
    }

    public void waitIdle() {
        VK10.vkQueueWaitIdle((VkQueue)this.vkQueue);
    }

    public CommandPool getCommandPool() {
        return this.commandPool;
    }

    public static QueueFamilyIndices getQueueFamilies() {
        if (device == null) {
            device = Vulkan.getVkDevice();
        }
        if (queueFamilyIndices == null) {
            queueFamilyIndices = Queue.findQueueFamilies(device.getPhysicalDevice());
        }
        return queueFamilyIndices;
    }

    public static QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device) {
        QueueFamilyIndices indices = new QueueFamilyIndices();
        try (MemoryStack stack = MemoryStack.stackPush();){
            int queueFlags;
            int i;
            IntBuffer queueFamilyCount = stack.ints(0);
            VK10.vkGetPhysicalDeviceQueueFamilyProperties((VkPhysicalDevice)device, (IntBuffer)queueFamilyCount, null);
            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.malloc((int)queueFamilyCount.get(0), (MemoryStack)stack);
            VK10.vkGetPhysicalDeviceQueueFamilyProperties((VkPhysicalDevice)device, (IntBuffer)queueFamilyCount, (VkQueueFamilyProperties.Buffer)queueFamilies);
            IntBuffer presentSupport = stack.ints(0);
            for (i = 0; i < queueFamilies.capacity(); ++i) {
                queueFlags = ((VkQueueFamilyProperties)queueFamilies.get(i)).queueFlags();
                if ((queueFlags & 1) != 0) {
                    indices.graphicsFamily = i;
                    KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR((VkPhysicalDevice)device, (int)i, (long)Vulkan.getSurface(), (IntBuffer)presentSupport);
                    if (presentSupport.get(0) == 1) {
                        indices.presentFamily = i;
                    }
                } else if ((queueFlags & 1) == 0 && (queueFlags & 2) != 0) {
                    indices.computeFamily = i;
                } else if ((queueFlags & 3) == 0 && (queueFlags & 4) != 0) {
                    indices.transferFamily = i;
                }
                if (indices.presentFamily == -1) {
                    KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR((VkPhysicalDevice)device, (int)i, (long)Vulkan.getSurface(), (IntBuffer)presentSupport);
                    if (presentSupport.get(0) == 1) {
                        indices.presentFamily = i;
                    }
                }
                if (indices.isComplete()) break;
            }
            if (indices.presentFamily == -1) {
                indices.presentFamily = indices.computeFamily;
                Initializer.LOGGER.warn("Using compute queue as present fallback");
            }
            if (indices.transferFamily == -1) {
                int transferIndex = -1;
                for (int i2 = 0; i2 < queueFamilies.capacity(); ++i2) {
                    int queueFlags2 = ((VkQueueFamilyProperties)queueFamilies.get(i2)).queueFlags();
                    if ((queueFlags2 & 4) == 0) continue;
                    if (transferIndex == -1) {
                        transferIndex = i2;
                    }
                    if ((queueFlags2 & 1) != 0) continue;
                    indices.transferFamily = i2;
                    if (i2 != indices.computeFamily) break;
                    transferIndex = i2;
                }
                if (transferIndex == -1) {
                    throw new RuntimeException("Failed to find queue family with transfer support");
                }
                indices.transferFamily = transferIndex;
            }
            if (indices.computeFamily == -1) {
                for (i = 0; i < queueFamilies.capacity(); ++i) {
                    queueFlags = ((VkQueueFamilyProperties)queueFamilies.get(i)).queueFlags();
                    if ((queueFlags & 2) == 0) continue;
                    indices.computeFamily = i;
                    break;
                }
            }
            if (indices.graphicsFamily == -1) {
                throw new RuntimeException("Unable to find queue family with graphics support.");
            }
            if (indices.presentFamily == -1) {
                throw new RuntimeException("Unable to find queue family with present support.");
            }
            if (indices.computeFamily == -1) {
                throw new RuntimeException("Unable to find queue family with compute support.");
            }
            QueueFamilyIndices queueFamilyIndices = indices;
            return queueFamilyIndices;
        }
    }

    public static class QueueFamilyIndices {
        public int graphicsFamily = -1;
        public int presentFamily = -1;
        public int transferFamily = -1;
        public int computeFamily = -1;

        public boolean isComplete() {
            return this.graphicsFamily != -1 && this.presentFamily != -1 && this.transferFamily != -1 && this.computeFamily != -1;
        }

        public boolean isSuitable() {
            return this.graphicsFamily != -1 && this.presentFamily != -1;
        }

        public int[] unique() {
            return IntStream.of(this.graphicsFamily, this.presentFamily, this.transferFamily, this.computeFamily).distinct().toArray();
        }

        public int[] array() {
            return new int[]{this.graphicsFamily, this.presentFamily};
        }
    }

    public static enum Family {
        Graphics,
        Transfer,
        Compute;

    }
}

