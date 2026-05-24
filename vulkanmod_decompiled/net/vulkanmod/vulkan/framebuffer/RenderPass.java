/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.Struct
 *  org.lwjgl.vulkan.KHRDynamicRendering
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VkAttachmentDescription
 *  org.lwjgl.vulkan.VkAttachmentDescription$Buffer
 *  org.lwjgl.vulkan.VkAttachmentReference
 *  org.lwjgl.vulkan.VkAttachmentReference$Buffer
 *  org.lwjgl.vulkan.VkClearValue
 *  org.lwjgl.vulkan.VkClearValue$Buffer
 *  org.lwjgl.vulkan.VkCommandBuffer
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkRect2D
 *  org.lwjgl.vulkan.VkRenderPassBeginInfo
 *  org.lwjgl.vulkan.VkRenderPassCreateInfo
 *  org.lwjgl.vulkan.VkRenderingAttachmentInfo
 *  org.lwjgl.vulkan.VkRenderingAttachmentInfo$Buffer
 *  org.lwjgl.vulkan.VkRenderingInfo
 *  org.lwjgl.vulkan.VkSubpassDependency
 *  org.lwjgl.vulkan.VkSubpassDependency$Buffer
 *  org.lwjgl.vulkan.VkSubpassDescription
 *  org.lwjgl.vulkan.VkSubpassDescription$Buffer
 */
package net.vulkanmod.vulkan.framebuffer;

import java.nio.LongBuffer;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Struct;
import org.lwjgl.vulkan.KHRDynamicRendering;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkRenderingAttachmentInfo;
import org.lwjgl.vulkan.VkRenderingInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;

public class RenderPass {
    Framebuffer framebuffer;
    long id;
    final int attachmentCount;
    AttachmentInfo colorAttachmentInfo;
    AttachmentInfo depthAttachmentInfo;

    public RenderPass(Framebuffer framebuffer, AttachmentInfo colorAttachmentInfo, AttachmentInfo depthAttachmentInfo) {
        this.framebuffer = framebuffer;
        this.colorAttachmentInfo = colorAttachmentInfo;
        this.depthAttachmentInfo = depthAttachmentInfo;
        int count = 0;
        if (colorAttachmentInfo != null) {
            ++count;
        }
        if (depthAttachmentInfo != null) {
            ++count;
        }
        this.attachmentCount = count;
    }

    private void createRenderPass() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc((int)this.attachmentCount, (MemoryStack)stack);
            VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.calloc((int)this.attachmentCount, (MemoryStack)stack);
            VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc((int)1, (MemoryStack)stack);
            subpass.pipelineBindPoint(0);
            int i = 0;
            if (this.colorAttachmentInfo != null) {
                VkAttachmentDescription colorAttachment = (VkAttachmentDescription)attachments.get(i);
                colorAttachment.format(this.colorAttachmentInfo.format).samples(1).loadOp(this.colorAttachmentInfo.loadOp).storeOp(this.colorAttachmentInfo.storeOp).stencilLoadOp(2).stencilStoreOp(1).initialLayout(2).finalLayout(this.colorAttachmentInfo.finalLayout);
                VkAttachmentReference colorAttachmentRef = ((VkAttachmentReference)attachmentRefs.get(0)).attachment(0).layout(2);
                subpass.colorAttachmentCount(1);
                subpass.pColorAttachments((VkAttachmentReference.Buffer)VkAttachmentReference.calloc((int)1, (MemoryStack)stack).put(0, (Struct)colorAttachmentRef));
                ++i;
            }
            if (this.depthAttachmentInfo != null) {
                VkAttachmentDescription depthAttachment = (VkAttachmentDescription)attachments.get(i);
                depthAttachment.format(this.depthAttachmentInfo.format).samples(1).loadOp(this.depthAttachmentInfo.loadOp).storeOp(this.depthAttachmentInfo.storeOp).stencilLoadOp(2).stencilStoreOp(1).initialLayout(3).finalLayout(this.depthAttachmentInfo.finalLayout);
                VkAttachmentReference depthAttachmentRef = ((VkAttachmentReference)attachmentRefs.get(1)).attachment(1).layout(3);
                subpass.pDepthStencilAttachment(depthAttachmentRef);
            }
            VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc((MemoryStack)stack);
            renderPassInfo.sType$Default().pAttachments(attachments).pSubpasses(subpass);
            switch (this.colorAttachmentInfo.finalLayout) {
                case 1000001002: {
                    VkSubpassDependency.Buffer subpassDependencies = VkSubpassDependency.calloc((int)1, (MemoryStack)stack);
                    ((VkSubpassDependency)subpassDependencies.get(0)).srcSubpass(-1).dstSubpass(0).srcStageMask(1024).dstStageMask(8192).srcAccessMask(0).dstAccessMask(0);
                    renderPassInfo.pDependencies(subpassDependencies);
                    break;
                }
                case 5: {
                    VkSubpassDependency.Buffer subpassDependencies = VkSubpassDependency.calloc((int)1, (MemoryStack)stack);
                    ((VkSubpassDependency)subpassDependencies.get(0)).srcSubpass(0).dstSubpass(-1).srcStageMask(1024).dstStageMask(128).srcAccessMask(256).dstAccessMask(32);
                    renderPassInfo.pDependencies(subpassDependencies);
                }
            }
            LongBuffer pRenderPass = stack.mallocLong(1);
            if (VK10.vkCreateRenderPass((VkDevice)Vulkan.getVkDevice(), (VkRenderPassCreateInfo)renderPassInfo, null, (LongBuffer)pRenderPass) != 0) {
                throw new RuntimeException("Failed to create render pass");
            }
            this.id = pRenderPass.get(0);
        }
    }

    public void beginRenderPass(VkCommandBuffer commandBuffer, long framebufferId, MemoryStack stack) {
        if (this.colorAttachmentInfo != null && this.framebuffer.getColorAttachment().getCurrentLayout() != 2) {
            this.framebuffer.getColorAttachment().transitionImageLayout(stack, commandBuffer, 2);
        }
        if (this.depthAttachmentInfo != null && this.framebuffer.getDepthAttachment().getCurrentLayout() != 3) {
            this.framebuffer.getDepthAttachment().transitionImageLayout(stack, commandBuffer, 3);
        }
        VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc((MemoryStack)stack);
        renderPassInfo.sType$Default();
        renderPassInfo.renderPass(this.id);
        renderPassInfo.framebuffer(framebufferId);
        VkRect2D renderArea = VkRect2D.malloc((MemoryStack)stack);
        renderArea.offset().set(0, 0);
        renderArea.extent().set(this.framebuffer.getWidth(), this.framebuffer.getHeight());
        renderPassInfo.renderArea(renderArea);
        VkClearValue.Buffer clearValues = VkClearValue.malloc((int)2, (MemoryStack)stack);
        ((VkClearValue)clearValues.get(0)).color().float32(VRenderSystem.clearColor);
        ((VkClearValue)clearValues.get(1)).depthStencil().set(1.0f, 0);
        renderPassInfo.pClearValues(clearValues);
        VK10.vkCmdBeginRenderPass((VkCommandBuffer)commandBuffer, (VkRenderPassBeginInfo)renderPassInfo, (int)0);
        Renderer.getInstance().setBoundRenderPass(this);
    }

    public void endRenderPass(VkCommandBuffer commandBuffer) {
        KHRDynamicRendering.vkCmdEndRenderingKHR((VkCommandBuffer)commandBuffer);
        try (MemoryStack stack = MemoryStack.stackPush();){
            if (this.colorAttachmentInfo != null && this.framebuffer.getColorAttachment().getCurrentLayout() != this.colorAttachmentInfo.finalLayout) {
                this.framebuffer.getColorAttachment().transitionImageLayout(stack, commandBuffer, this.colorAttachmentInfo.finalLayout);
            }
            if (this.depthAttachmentInfo != null && this.framebuffer.getDepthAttachment().getCurrentLayout() != this.depthAttachmentInfo.finalLayout) {
                this.framebuffer.getDepthAttachment().transitionImageLayout(stack, commandBuffer, this.depthAttachmentInfo.finalLayout);
            }
        }
        Renderer.getInstance().setBoundRenderPass(null);
    }

    public void beginDynamicRendering(VkCommandBuffer commandBuffer, MemoryStack stack) {
        if (this.colorAttachmentInfo != null && this.framebuffer.getColorAttachment().getCurrentLayout() != 2) {
            this.framebuffer.getColorAttachment().transitionImageLayout(stack, commandBuffer, 2);
        }
        if (this.depthAttachmentInfo != null && this.framebuffer.getDepthAttachment().getCurrentLayout() != 3) {
            this.framebuffer.getDepthAttachment().transitionImageLayout(stack, commandBuffer, 3);
        }
        VkRect2D renderArea = VkRect2D.malloc((MemoryStack)stack);
        renderArea.offset().set(0, 0);
        renderArea.extent().set(this.framebuffer.getWidth(), this.framebuffer.getHeight());
        VkClearValue.Buffer clearValues = VkClearValue.malloc((int)2, (MemoryStack)stack);
        ((VkClearValue)clearValues.get(0)).color().float32(VRenderSystem.clearColor);
        ((VkClearValue)clearValues.get(1)).depthStencil().set(1.0f, 0);
        VkRenderingInfo renderingInfo = VkRenderingInfo.calloc((MemoryStack)stack);
        renderingInfo.sType(1000044000);
        renderingInfo.renderArea(renderArea);
        renderingInfo.layerCount(1);
        if (this.colorAttachmentInfo != null) {
            VkRenderingAttachmentInfo.Buffer colorAttachment = VkRenderingAttachmentInfo.calloc((int)1, (MemoryStack)stack);
            colorAttachment.sType(1000044001);
            colorAttachment.imageView(this.framebuffer.getColorAttachmentView());
            colorAttachment.imageLayout(2);
            colorAttachment.loadOp(this.colorAttachmentInfo.loadOp);
            colorAttachment.storeOp(this.colorAttachmentInfo.storeOp);
            colorAttachment.clearValue((VkClearValue)clearValues.get(0));
            renderingInfo.pColorAttachments(colorAttachment);
        }
        if (this.depthAttachmentInfo != null) {
            VkRenderingAttachmentInfo depthAttachment = VkRenderingAttachmentInfo.calloc((MemoryStack)stack);
            depthAttachment.sType(1000044001);
            depthAttachment.imageView(this.framebuffer.getDepthImageView());
            depthAttachment.imageLayout(3);
            depthAttachment.loadOp(this.depthAttachmentInfo.loadOp);
            depthAttachment.storeOp(this.depthAttachmentInfo.storeOp);
            depthAttachment.clearValue((VkClearValue)clearValues.get(1));
            renderingInfo.pDepthAttachment(depthAttachment);
        }
        KHRDynamicRendering.vkCmdBeginRenderingKHR((VkCommandBuffer)commandBuffer, (VkRenderingInfo)renderingInfo);
    }

    public Framebuffer getFramebuffer() {
        return this.framebuffer;
    }

    public void setFramebuffer(Framebuffer framebuffer) {
        this.framebuffer = framebuffer;
    }

    public void cleanUp() {
    }

    public long getId() {
        return this.id;
    }

    public static Builder builder(Framebuffer framebuffer) {
        return new Builder(framebuffer);
    }

    private /* synthetic */ void lambda$cleanUp$0() {
        VK10.vkDestroyRenderPass((VkDevice)Vulkan.getVkDevice(), (long)this.id, null);
    }

    public static class AttachmentInfo {
        final Type type;
        final int format;
        int finalLayout;
        int loadOp;
        int storeOp;

        public AttachmentInfo(Type type, int format) {
            this.type = type;
            this.format = format;
            this.finalLayout = type.defaultLayout;
            this.loadOp = 2;
            this.storeOp = 0;
        }

        public AttachmentInfo setOps(int loadOp, int storeOp) {
            this.loadOp = loadOp;
            this.storeOp = storeOp;
            return this;
        }

        public AttachmentInfo setLoadOp(int loadOp) {
            this.loadOp = loadOp;
            return this;
        }

        public AttachmentInfo setFinalLayout(int finalLayout) {
            this.finalLayout = finalLayout;
            return this;
        }

        public static enum Type {
            COLOR(2),
            DEPTH(3);

            final int defaultLayout;

            private Type(int layout) {
                this.defaultLayout = layout;
            }
        }
    }

    public static class Builder {
        Framebuffer framebuffer;
        AttachmentInfo colorAttachmentInfo;
        AttachmentInfo depthAttachmentInfo;

        public Builder(Framebuffer framebuffer) {
            this.framebuffer = framebuffer;
            if (framebuffer.hasColorAttachment) {
                this.colorAttachmentInfo = new AttachmentInfo(AttachmentInfo.Type.COLOR, framebuffer.format).setOps(1, 0);
            }
            if (framebuffer.hasDepthAttachment) {
                this.depthAttachmentInfo = new AttachmentInfo(AttachmentInfo.Type.DEPTH, framebuffer.depthFormat).setOps(1, 1);
            }
        }

        public RenderPass build() {
            return new RenderPass(this.framebuffer, this.colorAttachmentInfo, this.depthAttachmentInfo);
        }

        public Builder setLoadOp(int loadOp) {
            if (this.colorAttachmentInfo != null) {
                this.colorAttachmentInfo.setLoadOp(loadOp);
            }
            if (this.depthAttachmentInfo != null) {
                this.depthAttachmentInfo.setLoadOp(loadOp);
            }
            return this;
        }

        public AttachmentInfo getColorAttachmentInfo() {
            return this.colorAttachmentInfo;
        }

        public AttachmentInfo getDepthAttachmentInfo() {
            return this.depthAttachmentInfo;
        }
    }
}

