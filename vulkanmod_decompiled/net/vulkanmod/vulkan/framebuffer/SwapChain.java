/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.vulkan.KHRSwapchain
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkExtent2D
 *  org.lwjgl.vulkan.VkFramebufferCreateInfo
 *  org.lwjgl.vulkan.VkPhysicalDevice
 *  org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR
 *  org.lwjgl.vulkan.VkSurfaceFormatKHR
 *  org.lwjgl.vulkan.VkSurfaceFormatKHR$Buffer
 *  org.lwjgl.vulkan.VkSwapchainCreateInfoKHR
 */
package net.vulkanmod.vulkan.framebuffer;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.vulkanmod.Initializer;
import net.vulkanmod.render.util.MathUtil;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import net.vulkanmod.vulkan.framebuffer.RenderPass;
import net.vulkanmod.vulkan.queue.Queue;
import net.vulkanmod.vulkan.texture.SamplerManager;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

public class SwapChain
extends Framebuffer {
    private static final int defUncappedMode = SwapChain.checkPresentMode(0, 1);
    private final Long2ReferenceOpenHashMap<long[]> FBO_map = new Long2ReferenceOpenHashMap();
    private long swapChainId = 0L;
    private List<VulkanImage> swapChainImages;
    private VkExtent2D extent2D;
    public boolean isBGRAformat;
    private boolean vsync = false;
    private boolean hasImages = false;

    public SwapChain() {
        this.attachmentCount = 2;
        this.depthFormat = Vulkan.getDefaultDepthFormat();
        this.hasColorAttachment = true;
        this.hasDepthAttachment = true;
        this.recreate();
    }

    public void recreate() {
        if (this.depthAttachment != null) {
            this.depthAttachment.free();
            this.depthAttachment = null;
        }
        this.createSwapChain();
    }

    private void createSwapChain() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkDevice device = Vulkan.getVkDevice();
            DeviceManager.SurfaceProperties surfaceProperties = DeviceManager.querySurfaceProperties(device.getPhysicalDevice(), stack);
            VkSurfaceFormatKHR surfaceFormat = this.getFormat(surfaceProperties.formats);
            int presentMode = this.getPresentMode(surfaceProperties.presentModes);
            VkExtent2D extent = SwapChain.getExtent(surfaceProperties.capabilities);
            if (extent.width() == 0 && extent.height() == 0) {
                if (this.swapChainId != 0L) {
                    this.swapChainImages.forEach(image -> VK10.vkDestroyImageView((VkDevice)device, (long)image.getImageView(), null));
                    KHRSwapchain.vkDestroySwapchainKHR((VkDevice)device, (long)this.swapChainId, null);
                    this.swapChainId = 0L;
                }
                this.width = 0;
                this.height = 0;
                this.hasImages = false;
                return;
            }
            int requestedImages = surfaceProperties.capabilities.minImageCount() + 1;
            if (surfaceProperties.capabilities.maxImageCount() > 0 && requestedImages > surfaceProperties.capabilities.maxImageCount()) {
                requestedImages = surfaceProperties.capabilities.maxImageCount();
            }
            IntBuffer imageCount = stack.ints(requestedImages);
            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc((MemoryStack)stack);
            createInfo.sType(1000001000);
            createInfo.surface(Vulkan.getSurface());
            this.format = surfaceFormat.format();
            this.extent2D = VkExtent2D.create().set(extent);
            createInfo.minImageCount(requestedImages);
            createInfo.imageFormat(this.format);
            createInfo.imageColorSpace(surfaceFormat.colorSpace());
            createInfo.imageExtent(extent);
            createInfo.imageArrayLayers(1);
            createInfo.imageUsage(20);
            Queue.QueueFamilyIndices indices = Queue.getQueueFamilies();
            if (indices.graphicsFamily != indices.presentFamily) {
                createInfo.imageSharingMode(1);
                createInfo.pQueueFamilyIndices(stack.ints(indices.graphicsFamily, indices.presentFamily));
            } else {
                createInfo.imageSharingMode(0);
            }
            createInfo.preTransform(surfaceProperties.capabilities.currentTransform());
            createInfo.compositeAlpha(1);
            createInfo.presentMode(presentMode);
            createInfo.clipped(true);
            if (this.swapChainId != 0L) {
                this.swapChainImages.forEach(image -> VK10.vkDestroyImageView((VkDevice)device, (long)image.getImageView(), null));
                KHRSwapchain.vkDestroySwapchainKHR((VkDevice)device, (long)this.swapChainId, null);
            }
            LongBuffer pSwapChain = stack.longs(0L);
            int result = KHRSwapchain.vkCreateSwapchainKHR((VkDevice)device, (VkSwapchainCreateInfoKHR)createInfo, null, (LongBuffer)pSwapChain);
            Vulkan.checkResult(result, "Failed to create swap chain");
            this.swapChainId = pSwapChain.get(0);
            KHRSwapchain.vkGetSwapchainImagesKHR((VkDevice)device, (long)this.swapChainId, (IntBuffer)imageCount, null);
            LongBuffer pSwapchainImages = stack.mallocLong(imageCount.get(0));
            KHRSwapchain.vkGetSwapchainImagesKHR((VkDevice)device, (long)this.swapChainId, (IntBuffer)imageCount, (LongBuffer)pSwapchainImages);
            this.swapChainImages = new ArrayList<VulkanImage>(imageCount.get(0));
            this.hasImages = true;
            this.width = this.extent2D.width();
            this.height = this.extent2D.height();
            for (int i = 0; i < pSwapchainImages.capacity(); ++i) {
                long imageId = pSwapchainImages.get(i);
                long imageView = VulkanImage.createImageView(imageId, this.format, 1, 1, 1);
                VulkanImage image2 = new VulkanImage("Swapchain", imageId, this.format, 1, this.width, this.height, 4, 0, imageView);
                long samplerId = SamplerManager.getSampler(true, true, 0);
                image2.setSampler(samplerId);
                this.swapChainImages.add(image2);
            }
        }
        this.createDepthResources();
    }

    private long[] createFramebuffers(RenderPass renderPass) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            long[] framebuffers = new long[this.swapChainImages.size()];
            for (int i = 0; i < this.swapChainImages.size(); ++i) {
                LongBuffer attachments = stack.longs(this.swapChainImages.get(i).getImageView(), this.depthAttachment.getImageView());
                LongBuffer pFramebuffer = stack.mallocLong(1);
                VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc((MemoryStack)stack);
                framebufferInfo.sType(37);
                framebufferInfo.renderPass(renderPass.getId());
                framebufferInfo.width(this.width);
                framebufferInfo.height(this.height);
                framebufferInfo.layers(1);
                framebufferInfo.pAttachments(attachments);
                if (VK10.vkCreateFramebuffer((VkDevice)Vulkan.getVkDevice(), (VkFramebufferCreateInfo)framebufferInfo, null, (LongBuffer)pFramebuffer) != 0) {
                    throw new RuntimeException("Failed to create framebuffer");
                }
                framebuffers[i] = pFramebuffer.get(0);
            }
            long[] lArray = framebuffers;
            return lArray;
        }
    }

    private void createDepthResources() {
        this.depthAttachment = VulkanImage.createDepthImage(this.depthFormat, this.width, this.height, 36, false, false);
    }

    @Override
    protected long getFramebufferId(RenderPass renderPass) {
        long[] framebuffers = (long[])this.FBO_map.computeIfAbsent(renderPass.id, renderPass1 -> this.createFramebuffers(renderPass));
        return framebuffers[Renderer.getCurrentImage()];
    }

    @Override
    public void cleanUp() {
        VkDevice device = Vulkan.getVkDevice();
        KHRSwapchain.vkDestroySwapchainKHR((VkDevice)device, (long)this.swapChainId, null);
        this.swapChainImages.forEach(image -> VK10.vkDestroyImageView((VkDevice)device, (long)image.getImageView(), null));
        this.depthAttachment.free();
    }

    private VkSurfaceFormatKHR getFormat(VkSurfaceFormatKHR.Buffer availableFormats) {
        List list = availableFormats.stream().toList();
        VkSurfaceFormatKHR format = (VkSurfaceFormatKHR)list.get(0);
        for (VkSurfaceFormatKHR availableFormat : list) {
            if (availableFormat.format() == 37 && availableFormat.colorSpace() == 0) {
                return availableFormat;
            }
            if (availableFormat.format() != 44 || availableFormat.colorSpace() != 0) continue;
            format = availableFormat;
        }
        if (format.format() == 44) {
            this.isBGRAformat = true;
        }
        return format;
    }

    private int getPresentMode(IntBuffer availablePresentModes) {
        int requestedMode;
        int n = requestedMode = this.vsync ? 2 : defUncappedMode;
        if (requestedMode == 2) {
            return 2;
        }
        for (int i = 0; i < availablePresentModes.capacity(); ++i) {
            if (availablePresentModes.get(i) != requestedMode) continue;
            return requestedMode;
        }
        Initializer.LOGGER.warn("Requested mode not supported: " + this.getDisplayModeString(requestedMode) + ": using FIFO present mode");
        return 2;
    }

    private String getDisplayModeString(int requestedMode) {
        return switch (requestedMode) {
            case 0 -> "Immediate";
            case 1 -> "Mailbox (FastSync)";
            case 3 -> "FIFO Relaxed (Adaptive VSync)";
            default -> "FIFO (VSync)";
        };
    }

    private static VkExtent2D getExtent(VkSurfaceCapabilitiesKHR capabilities) {
        if (capabilities.currentExtent().width() != -1) {
            return capabilities.currentExtent();
        }
        IntBuffer width = MemoryStack.stackGet().ints(0);
        IntBuffer height = MemoryStack.stackGet().ints(0);
        GLFW.glfwGetFramebufferSize((long)Vulkan.window, (IntBuffer)width, (IntBuffer)height);
        VkExtent2D actualExtent = VkExtent2D.malloc((MemoryStack)MemoryStack.stackPush()).set(width.get(0), height.get(0));
        VkExtent2D minExtent = capabilities.minImageExtent();
        VkExtent2D maxExtent = capabilities.maxImageExtent();
        actualExtent.width(MathUtil.clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
        actualExtent.height(MathUtil.clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));
        return actualExtent;
    }

    private static int checkPresentMode(int ... requestedModes) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer a = DeviceManager.querySurfaceProperties((VkPhysicalDevice)DeviceManager.vkDevice.getPhysicalDevice(), (MemoryStack)stack).presentModes;
            for (int dMode : requestedModes) {
                for (int i = 0; i < a.capacity(); ++i) {
                    if (a.get(i) != dMode) continue;
                    int n = dMode;
                    return n;
                }
            }
            int n = 2;
            return n;
        }
    }

    public long getId() {
        return this.swapChainId;
    }

    public List<VulkanImage> getImages() {
        return this.swapChainImages;
    }

    public long getImageId(int i) {
        return this.swapChainImages.get(i).getId();
    }

    public VkExtent2D getExtent() {
        return this.extent2D;
    }

    @Override
    public VulkanImage getColorAttachment() {
        return this.swapChainImages.get(Renderer.getCurrentImage());
    }

    @Override
    public long getColorAttachmentView() {
        return this.getColorAttachment().getImageView();
    }

    public boolean hasImages() {
        return this.hasImages;
    }

    public boolean isVsync() {
        return this.vsync;
    }

    public void setVsync(boolean vsync) {
        this.vsync = vsync;
    }

    public int getImagesNum() {
        return this.swapChainImages.size();
    }

    private static /* synthetic */ void lambda$cleanUp$6(long pass, long[] framebuffers) {
        Arrays.stream(framebuffers).forEach(id -> VK10.vkDestroyFramebuffer((VkDevice)Vulkan.getVkDevice(), (long)id, null));
    }

    private static /* synthetic */ void lambda$recreate$1(long pass, long[] framebuffers) {
        Arrays.stream(framebuffers).forEach(id -> VK10.vkDestroyFramebuffer((VkDevice)Vulkan.getVkDevice(), (long)id, null));
    }
}

