/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VkBufferImageCopy
 *  org.lwjgl.vulkan.VkBufferImageCopy$Buffer
 *  org.lwjgl.vulkan.VkCommandBuffer
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkExtent3D
 *  org.lwjgl.vulkan.VkImageBlit
 *  org.lwjgl.vulkan.VkImageBlit$Buffer
 *  org.lwjgl.vulkan.VkImageMemoryBarrier
 *  org.lwjgl.vulkan.VkImageMemoryBarrier$Buffer
 *  org.lwjgl.vulkan.VkOffset3D
 */
package net.vulkanmod.vulkan.texture;

import java.nio.LongBuffer;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import net.vulkanmod.vulkan.queue.CommandPool;
import net.vulkanmod.vulkan.texture.VulkanImage;
import net.vulkanmod.vulkan.util.VUtil;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferImageCopy;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent3D;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkOffset3D;

public abstract class ImageUtil {
    public static void copyBufferToImageCmd(MemoryStack stack, VkCommandBuffer commandBuffer, long buffer, long image, int arrayLayer, int mipLevel, int width, int height, int xOffset, int yOffset, int bufferOffset, int bufferRowLenght, int bufferImageHeight) {
        VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc((int)1, (MemoryStack)stack);
        region.bufferOffset((long)bufferOffset);
        region.bufferRowLength(bufferRowLenght);
        region.bufferImageHeight(bufferImageHeight);
        region.imageSubresource().aspectMask(1);
        region.imageSubresource().mipLevel(mipLevel);
        region.imageSubresource().baseArrayLayer(arrayLayer);
        region.imageSubresource().layerCount(1);
        region.imageOffset().set(xOffset, yOffset, 0);
        region.imageExtent(VkExtent3D.calloc((MemoryStack)stack).set(width, height, 1));
        VK10.vkCmdCopyBufferToImage((VkCommandBuffer)commandBuffer, (long)buffer, (long)image, (int)7, (VkBufferImageCopy.Buffer)region);
    }

    public static void downloadTexture(VulkanImage image, long ptr) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            int prevLayout = image.getCurrentLayout();
            CommandPool.CommandBuffer commandBuffer = DeviceManager.getGraphicsQueue().beginCommands();
            image.transitionImageLayout(stack, commandBuffer.getHandle(), 6);
            long imageSize = (long)image.width * (long)image.height * (long)image.formatSize;
            LongBuffer pStagingBuffer = stack.mallocLong(1);
            PointerBuffer pStagingAllocation = stack.pointers(0L);
            MemoryManager.getInstance().createBuffer(imageSize, 2, 6, pStagingBuffer, pStagingAllocation);
            ImageUtil.copyImageToBufferCmd(stack, commandBuffer.getHandle(), pStagingBuffer.get(0), image.getId(), 0, image.width, image.height, 0, 0, 0L, 0, 0);
            image.transitionImageLayout(stack, commandBuffer.getHandle(), prevLayout);
            long fence = DeviceManager.getGraphicsQueue().submitCommands(commandBuffer);
            VK10.vkWaitForFences((VkDevice)DeviceManager.vkDevice, (long)fence, (boolean)true, (long)-1L);
            MemoryManager.MapAndCopy(pStagingAllocation.get(0), data -> VUtil.memcpy(data.getByteBuffer(0, (int)imageSize), ptr));
            MemoryManager.freeBuffer(pStagingBuffer.get(0), pStagingAllocation.get(0));
        }
    }

    public static void copyImageToBuffer(VulkanImage image, Buffer buffer, int mipLevel, int width, int height, int xOffset, int yOffset, long bufferOffset, int bufferRowLength, int bufferImageHeight) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            int prevLayout = image.getCurrentLayout();
            CommandPool.CommandBuffer commandBuffer = DeviceManager.getGraphicsQueue().beginCommands();
            image.transitionImageLayout(stack, commandBuffer.getHandle(), 6);
            ImageUtil.copyImageToBufferCmd(stack, commandBuffer.getHandle(), buffer.getId(), image.getId(), mipLevel, width, height, xOffset, yOffset, bufferOffset, bufferRowLength, bufferImageHeight);
            image.transitionImageLayout(stack, commandBuffer.getHandle(), prevLayout);
            long fence = DeviceManager.getGraphicsQueue().submitCommands(commandBuffer);
            VK10.vkWaitForFences((VkDevice)DeviceManager.vkDevice, (long)fence, (boolean)true, (long)-1L);
        }
    }

    public static void copyImageToBufferCmd(MemoryStack stack, VkCommandBuffer commandBuffer, long buffer, long image, int mipLevel, int width, int height, int xOffset, int yOffset, long bufferOffset, int bufferRowLength, int bufferImageHeight) {
        VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc((int)1, (MemoryStack)stack);
        region.bufferOffset(bufferOffset);
        region.bufferRowLength(bufferRowLength);
        region.bufferImageHeight(bufferImageHeight);
        region.imageSubresource().aspectMask(1);
        region.imageSubresource().mipLevel(mipLevel);
        region.imageSubresource().baseArrayLayer(0);
        region.imageSubresource().layerCount(1);
        region.imageOffset().set(xOffset, yOffset, 0);
        region.imageExtent().set(width, height, 1);
        VK10.vkCmdCopyImageToBuffer((VkCommandBuffer)commandBuffer, (long)image, (int)6, (long)buffer, (VkBufferImageCopy.Buffer)region);
    }

    public static void blitFramebuffer(VulkanImage dstImage, int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkCommandBuffer commandBuffer = Renderer.getCommandBuffer();
            Renderer.getInstance().endRenderPass(commandBuffer);
            dstImage.transitionImageLayout(stack, commandBuffer, 7);
            VulkanImage srcImage = Renderer.getInstance().getSwapChain().getColorAttachment();
            srcImage.transitionImageLayout(stack, commandBuffer, 6);
            VkImageBlit.Buffer blit = VkImageBlit.calloc((int)1, (MemoryStack)stack);
            blit.srcOffsets(0, VkOffset3D.calloc((MemoryStack)stack).set(0, 0, 0));
            blit.srcOffsets(1, VkOffset3D.calloc((MemoryStack)stack).set(srcImage.width, srcImage.height, 1));
            blit.srcSubresource().aspectMask(1).mipLevel(0).baseArrayLayer(0).layerCount(1);
            blit.dstOffsets(0, VkOffset3D.calloc((MemoryStack)stack).set(0, 0, 0));
            blit.dstOffsets(1, VkOffset3D.calloc((MemoryStack)stack).set(dstImage.width, dstImage.height, 1));
            blit.dstSubresource().aspectMask(1).mipLevel(0).baseArrayLayer(0).layerCount(1);
            VK10.vkCmdBlitImage((VkCommandBuffer)commandBuffer, (long)srcImage.getId(), (int)6, (long)dstImage.getId(), (int)7, (VkImageBlit.Buffer)blit, (int)1);
            dstImage.transitionImageLayout(stack, commandBuffer, 5);
            Renderer.getInstance().getMainPass().rebindMainTarget();
        }
    }

    public static void generateMipmaps(VulkanImage image) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkImageMemoryBarrier.Buffer barrier;
            CommandPool.CommandBuffer commandBuffer = DeviceManager.getGraphicsQueue().beginCommands();
            image.transitionImageLayout(stack, commandBuffer.getHandle(), 7);
            for (int level = 1; level < image.mipLevels; ++level) {
                int prevLevel = level - 1;
                barrier = VkImageMemoryBarrier.calloc((int)1, (MemoryStack)stack);
                barrier.sType(45);
                barrier.oldLayout(2);
                barrier.newLayout(1);
                barrier.srcQueueFamilyIndex(-1);
                barrier.dstQueueFamilyIndex(-1);
                barrier.image(image.getId());
                barrier.subresourceRange().baseMipLevel(prevLevel);
                barrier.subresourceRange().levelCount(1);
                barrier.subresourceRange().baseArrayLayer(0);
                barrier.subresourceRange().layerCount(-1);
                barrier.subresourceRange().aspectMask(image.aspect);
                barrier.srcAccessMask(4096);
                barrier.dstAccessMask(2048);
                VK10.vkCmdPipelineBarrier((VkCommandBuffer)commandBuffer.getHandle(), (int)4096, (int)4096, (int)0, null, null, (VkImageMemoryBarrier.Buffer)barrier);
                prevLevel = level - 1;
                VkImageBlit.Buffer blit = VkImageBlit.calloc((int)1, (MemoryStack)stack);
                blit.srcOffsets(0, VkOffset3D.calloc((MemoryStack)stack).set(0, 0, 0));
                blit.srcOffsets(1, VkOffset3D.calloc((MemoryStack)stack).set(image.width >> prevLevel, image.height >> prevLevel, 1));
                blit.srcSubresource().aspectMask(1).mipLevel(prevLevel).baseArrayLayer(0).layerCount(1);
                blit.dstOffsets(0, VkOffset3D.calloc((MemoryStack)stack).set(0, 0, 0));
                blit.dstOffsets(1, VkOffset3D.calloc((MemoryStack)stack).set(image.width >> level, image.height >> level, 1));
                blit.dstSubresource().aspectMask(1).mipLevel(level).baseArrayLayer(0).layerCount(1);
                VK10.vkCmdBlitImage((VkCommandBuffer)commandBuffer.getHandle(), (long)image.getId(), (int)6, (long)image.getId(), (int)7, (VkImageBlit.Buffer)blit, (int)1);
            }
            barrier = VkImageMemoryBarrier.calloc((int)1, (MemoryStack)stack);
            barrier.sType(45);
            barrier.oldLayout(1);
            barrier.newLayout(5);
            barrier.srcQueueFamilyIndex(-1);
            barrier.dstQueueFamilyIndex(-1);
            barrier.image(image.getId());
            barrier.subresourceRange().baseMipLevel(0);
            barrier.subresourceRange().levelCount(image.mipLevels - 1);
            barrier.subresourceRange().baseArrayLayer(0);
            barrier.subresourceRange().layerCount(-1);
            barrier.subresourceRange().aspectMask(image.aspect);
            barrier.srcAccessMask(4096);
            barrier.dstAccessMask(32);
            VK10.vkCmdPipelineBarrier((VkCommandBuffer)commandBuffer.getHandle(), (int)4096, (int)8192, (int)0, null, null, (VkImageMemoryBarrier.Buffer)barrier);
            barrier.oldLayout(2);
            barrier.subresourceRange().baseMipLevel(image.mipLevels - 1);
            barrier.subresourceRange().levelCount(1);
            VK10.vkCmdPipelineBarrier((VkCommandBuffer)commandBuffer.getHandle(), (int)4096, (int)8192, (int)0, null, null, (VkImageMemoryBarrier.Buffer)barrier);
            image.setCurrentLayout(5);
            long fence = DeviceManager.getGraphicsQueue().submitCommands(commandBuffer);
            VK10.vkWaitForFences((VkDevice)DeviceManager.vkDevice, (long)fence, (boolean)true, (long)-1L);
        }
    }
}

