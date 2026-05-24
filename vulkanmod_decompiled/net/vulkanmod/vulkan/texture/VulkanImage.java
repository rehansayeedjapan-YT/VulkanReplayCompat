/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VkCommandBuffer
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkImageMemoryBarrier
 *  org.lwjgl.vulkan.VkImageMemoryBarrier$Buffer
 *  org.lwjgl.vulkan.VkImageViewCreateInfo
 */
package net.vulkanmod.vulkan.texture;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import net.vulkanmod.render.texture.ImageUploadHelper;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.memory.buffer.StagingBuffer;
import net.vulkanmod.vulkan.queue.CommandPool;
import net.vulkanmod.vulkan.texture.ImageUtil;
import net.vulkanmod.vulkan.texture.SamplerManager;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageMemoryBarrier;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

public class VulkanImage {
    public static int DefaultFormat = 37;
    private static final VkDevice DEVICE = Vulkan.getVkDevice();
    public final String name;
    public final int format;
    public final int aspect;
    public final int arrayLayers;
    public final int mipLevels;
    public final int width;
    public final int height;
    public final int formatSize;
    public final int usage;
    public final int viewType;
    public final int size;
    private long id;
    private long allocation;
    private long mainImageView;
    private final long[] levelImageViews;
    private long sampler;
    private int currentLayout;

    public VulkanImage(String name, long id, int format, int mipLevels, int width, int height, int formatSize, int usage, long imageView) {
        this.id = id;
        this.mainImageView = imageView;
        this.name = name;
        this.arrayLayers = 1;
        this.mipLevels = mipLevels;
        this.width = width;
        this.height = height;
        this.formatSize = formatSize;
        this.format = format;
        this.usage = usage;
        this.aspect = VulkanImage.getAspect(this.format);
        this.viewType = 1;
        this.size = width * height * formatSize;
        this.levelImageViews = new long[mipLevels];
        this.sampler = SamplerManager.getDefaultSampler();
    }

    private VulkanImage(Builder builder) {
        this.name = builder.name;
        this.mipLevels = builder.mipLevels;
        this.width = builder.width;
        this.height = builder.height;
        this.arrayLayers = builder.arrayLayers;
        this.formatSize = builder.formatSize;
        this.format = builder.format;
        this.usage = builder.usage;
        this.aspect = VulkanImage.getAspect(this.format);
        this.viewType = builder.viewType;
        this.size = this.width * this.height * this.formatSize;
        this.levelImageViews = new long[builder.mipLevels];
    }

    public static VulkanImage createTextureImage(Builder builder) {
        VulkanImage image = new VulkanImage(builder);
        image.createImage();
        image.mainImageView = VulkanImage.createImageView(image.id, image.viewType, image.format, image.aspect, image.arrayLayers, 0, image.mipLevels);
        image.sampler = SamplerManager.getSampler(builder.clamp, builder.linearFiltering, builder.mipLevels - 1);
        return image;
    }

    public static VulkanImage createDepthImage(int format, int width, int height, int usage, boolean blur, boolean clamp) {
        VulkanImage image = VulkanImage.builder(width, height).setFormat(format).setUsage(usage).setLinearFiltering(blur).setClamp(clamp).createVulkanImage();
        return image;
    }

    public static VulkanImage createWhiteTexture() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            int i = -1;
            ByteBuffer buffer = stack.malloc(4);
            buffer.putInt(0, i);
            VulkanImage image = VulkanImage.builder(1, 1).setFormat(DefaultFormat).setUsage(6).setLinearFiltering(false).setClamp(false).createVulkanImage();
            image.uploadSubTextureAsync(0, 0, image.width, image.height, 0, 0, 0, 0, 0, buffer);
            VulkanImage vulkanImage = image;
            return vulkanImage;
        }
    }

    private void createImage() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            LongBuffer pTextureImage = stack.mallocLong(1);
            PointerBuffer pAllocation = stack.pointers(0L);
            int flags = this.viewType == 3 ? 16 : 0;
            MemoryManager.getInstance().createImage(this.width, this.height, this.arrayLayers, this.mipLevels, this.format, 0, this.usage, flags, 1, pTextureImage, pAllocation);
            this.id = pTextureImage.get(0);
            this.allocation = pAllocation.get(0);
            MemoryManager.addImage(this);
            if (this.name != null) {
                Vulkan.setDebugLabel(stack, 10, pTextureImage.get(), this.name);
            }
        }
    }

    public static int getAspect(int format) {
        return switch (format) {
            case 129, 130 -> 6;
            case 124, 125, 126 -> 2;
            default -> 1;
        };
    }

    public static boolean isDepthFormat(int format) {
        return switch (format) {
            case 124, 125, 126, 129, 130 -> true;
            default -> false;
        };
    }

    public static long createImageView(long image, int format, int aspectFlags, int arrayLayers, int mipLevels) {
        return VulkanImage.createImageView(image, 1, format, aspectFlags, arrayLayers, 0, mipLevels);
    }

    public static long createImageView(long image, int viewType, int format, int aspectFlags, int arrayLayers, int baseMipLevel, int mipLevels) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc((MemoryStack)stack);
            viewInfo.sType(15);
            viewInfo.image(image);
            viewInfo.viewType(viewType);
            viewInfo.format(format);
            viewInfo.subresourceRange().aspectMask(aspectFlags);
            viewInfo.subresourceRange().baseMipLevel(baseMipLevel);
            viewInfo.subresourceRange().levelCount(mipLevels);
            viewInfo.subresourceRange().baseArrayLayer(0);
            viewInfo.subresourceRange().layerCount(arrayLayers);
            LongBuffer pImageView = stack.mallocLong(1);
            if (VK10.vkCreateImageView((VkDevice)DEVICE, (VkImageViewCreateInfo)viewInfo, null, (LongBuffer)pImageView) != 0) {
                throw new RuntimeException("Failed to create texture image view");
            }
            long l = pImageView.get(0);
            return l;
        }
    }

    public void uploadSubTextureAsync(int mipLevel, int width, int height, int xOffset, int yOffset, int unpackSkipRows, int unpackSkipPixels, int unpackRowLength, ByteBuffer buffer) {
        this.uploadSubTextureAsync(mipLevel, 0, width, height, xOffset, yOffset, unpackSkipRows, unpackSkipPixels, unpackRowLength, MemoryUtil.memAddress((ByteBuffer)buffer));
    }

    public void uploadSubTextureAsync(int mipLevel, int arrayLayer, int width, int height, int xOffset, int yOffset, int unpackSkipRows, int unpackSkipPixels, int unpackRowLength, ByteBuffer buffer) {
        this.uploadSubTextureAsync(mipLevel, arrayLayer, width, height, xOffset, yOffset, unpackSkipRows, unpackSkipPixels, unpackRowLength, MemoryUtil.memAddress((ByteBuffer)buffer));
    }

    public void uploadSubTextureAsync(int mipLevel, int arrayLayer, int width, int height, int xOffset, int yOffset, int unpackSkipRows, int unpackSkipPixels, int unpackRowLength, long srcPtr) {
        long uploadSize = (long)(unpackRowLength * height - unpackSkipPixels) * (long)this.formatSize;
        StagingBuffer stagingBuffer = Vulkan.getStagingBuffer();
        if (uploadSize > stagingBuffer.getBufferSize()) {
            stagingBuffer = new StagingBuffer(uploadSize);
            stagingBuffer.scheduleFree();
        }
        stagingBuffer.align(this.formatSize);
        stagingBuffer.copyBuffer((int)uploadSize, srcPtr += ((long)unpackRowLength * (long)unpackSkipRows + (long)unpackSkipPixels) * (long)this.formatSize);
        long bufferId = stagingBuffer.getId();
        VkCommandBuffer commandBuffer = ImageUploadHelper.INSTANCE.getOrStartCommandBuffer().getHandle();
        try (MemoryStack stack = MemoryStack.stackPush();){
            this.transferDstLayout(stack, commandBuffer);
            int srcOffset = (int)stagingBuffer.getOffset();
            ImageUtil.copyBufferToImageCmd(stack, commandBuffer, bufferId, this.id, arrayLayer, mipLevel, width, height, xOffset, yOffset, srcOffset, unpackRowLength, height);
        }
    }

    private void transferDstLayout(MemoryStack stack, VkCommandBuffer commandBuffer) {
        this.transitionImageLayout(stack, commandBuffer, 7);
    }

    public void readOnlyLayout() {
        if (this.currentLayout == 5) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush();){
            if (Renderer.getInstance().getBoundRenderPass() != null) {
                CommandPool.CommandBuffer commandBuffer = ImageUploadHelper.INSTANCE.getOrStartCommandBuffer();
                VkCommandBuffer vkCommandBuffer = commandBuffer.getHandle();
                this.readOnlyLayout(stack, vkCommandBuffer);
            } else {
                this.readOnlyLayout(stack, Renderer.getCommandBuffer());
            }
        }
    }

    public void readOnlyLayout(MemoryStack stack, VkCommandBuffer commandBuffer) {
        this.transitionImageLayout(stack, commandBuffer, 5);
    }

    public void setSampler(long sampler) {
        this.sampler = sampler;
    }

    public void transitionImageLayout(MemoryStack stack, VkCommandBuffer commandBuffer, int newLayout) {
        VulkanImage.transitionImageLayout(stack, commandBuffer, this, newLayout);
    }

    public static void transitionImageLayout(MemoryStack stack, VkCommandBuffer commandBuffer, VulkanImage image, int newLayout) {
        int srcAccessMask;
        if (image.currentLayout == newLayout) {
            return;
        }
        int dstAccessMask = 0;
        VulkanImage.transitionLayout(stack, commandBuffer, image, image.currentLayout, newLayout, switch (image.currentLayout) {
            case 0, 1000001002 -> {
                srcAccessMask = 0;
                yield 8192;
            }
            case 7 -> {
                srcAccessMask = 4096;
                yield 4096;
            }
            case 6 -> {
                srcAccessMask = 2048;
                yield 4096;
            }
            case 5 -> {
                srcAccessMask = 32;
                yield 128;
            }
            case 2 -> {
                srcAccessMask = 256;
                yield 1024;
            }
            case 3 -> {
                srcAccessMask = 1536;
                yield 512;
            }
            default -> throw new RuntimeException("Unexpected value:" + image.currentLayout);
        }, srcAccessMask, switch (newLayout) {
            case 7 -> {
                dstAccessMask = 4096;
                yield 4096;
            }
            case 6 -> {
                dstAccessMask = 2048;
                yield 4096;
            }
            case 5 -> {
                dstAccessMask = 32;
                yield 8;
            }
            case 2 -> {
                dstAccessMask = 384;
                yield 1024;
            }
            case 3 -> {
                dstAccessMask = 1536;
                yield 256;
            }
            case 1000001002 -> 8192;
            default -> throw new RuntimeException("Unexpected value:" + newLayout);
        }, dstAccessMask);
    }

    public static void transitionLayout(MemoryStack stack, VkCommandBuffer commandBuffer, VulkanImage image, int oldLayout, int newLayout, int sourceStage, int srcAccessMask, int destinationStage, int dstAccessMask) {
        VulkanImage.transitionLayout(stack, commandBuffer, image, 0, oldLayout, newLayout, sourceStage, srcAccessMask, destinationStage, dstAccessMask);
    }

    public static void transitionLayout(MemoryStack stack, VkCommandBuffer commandBuffer, VulkanImage image, int baseLevel, int oldLayout, int newLayout, int sourceStage, int srcAccessMask, int destinationStage, int dstAccessMask) {
        VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc((int)1, (MemoryStack)stack);
        barrier.sType(45);
        barrier.oldLayout(image.currentLayout);
        barrier.newLayout(newLayout);
        barrier.srcQueueFamilyIndex(-1);
        barrier.dstQueueFamilyIndex(-1);
        barrier.image(image.getId());
        barrier.subresourceRange().baseMipLevel(baseLevel);
        barrier.subresourceRange().levelCount(-1);
        barrier.subresourceRange().baseArrayLayer(0);
        barrier.subresourceRange().layerCount(-1);
        barrier.subresourceRange().aspectMask(image.aspect);
        barrier.srcAccessMask(srcAccessMask);
        barrier.dstAccessMask(dstAccessMask);
        VK10.vkCmdPipelineBarrier((VkCommandBuffer)commandBuffer, (int)sourceStage, (int)destinationStage, (int)0, null, null, (VkImageMemoryBarrier.Buffer)barrier);
        image.currentLayout = newLayout;
    }

    private static boolean hasStencilComponent(int format) {
        return format == 130 || format == 129;
    }

    public void free() {
        MemoryManager.getInstance().addToFreeable(this);
    }

    public void doFree() {
        if (this.id == 0L) {
            return;
        }
        MemoryManager.freeImage(this.id, this.allocation);
        VK10.vkDestroyImageView((VkDevice)Vulkan.getVkDevice(), (long)this.mainImageView, null);
        if (this.levelImageViews != null) {
            Arrays.stream(this.levelImageViews).forEach(imageView -> {
                if (imageView != 0L) {
                    VK10.vkDestroyImageView((VkDevice)Vulkan.getVkDevice(), (long)imageView, null);
                }
            });
        }
        this.id = 0L;
    }

    public int getCurrentLayout() {
        return this.currentLayout;
    }

    public void setCurrentLayout(int currentLayout) {
        this.currentLayout = currentLayout;
    }

    public long getId() {
        return this.id;
    }

    public long getAllocation() {
        return this.allocation;
    }

    public long getImageView() {
        return this.mainImageView;
    }

    public long getLevelImageView(int i) {
        if (this.levelImageViews[i] == 0L) {
            this.levelImageViews[i] = VulkanImage.createImageView(this.id, 1, this.format, this.aspect, this.arrayLayers, i, 1);
        }
        return this.levelImageViews[i];
    }

    public long[] getLevelImageViews() {
        return this.levelImageViews;
    }

    public long getSampler() {
        return this.sampler;
    }

    public static Builder builder(int width, int height) {
        return new Builder(width, height);
    }

    public static class Builder {
        final int width;
        final int height;
        String name;
        int format = DefaultFormat;
        int formatSize;
        int arrayLayers = 1;
        byte mipLevels = 1;
        int usage = 7;
        int viewType = 1;
        boolean linearFiltering = false;
        boolean clamp = false;
        int reductionMode = -1;

        public Builder(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setFormat(int format) {
            this.format = format;
            return this;
        }

        public Builder setArrayLayers(int n) {
            this.arrayLayers = (byte)n;
            return this;
        }

        public Builder setMipLevels(int n) {
            this.mipLevels = (byte)n;
            return this;
        }

        public Builder setUsage(int usage) {
            this.usage = usage;
            return this;
        }

        public Builder addUsage(int usage) {
            this.usage |= usage;
            return this;
        }

        public Builder setViewType(int viewType) {
            this.viewType = viewType;
            return this;
        }

        public Builder setLinearFiltering(boolean b) {
            this.linearFiltering = b;
            return this;
        }

        public Builder setClamp(boolean b) {
            this.clamp = b;
            return this;
        }

        public Builder setSamplerReductionMode(int reductionMode) {
            this.reductionMode = reductionMode;
            return this;
        }

        public VulkanImage createVulkanImage() {
            this.formatSize = Builder.formatSize(this.format);
            return VulkanImage.createTextureImage(this);
        }

        private static int formatSize(int format) {
            return switch (format) {
                case 37, 41, 42, 43, 100, 126, 129 -> 4;
                case 76 -> 2;
                case 9 -> 1;
                case 97 -> 8;
                default -> throw new IllegalArgumentException(String.format("Unxepcted format: %s", format));
            };
        }
    }
}

