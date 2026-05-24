/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.opengl.GlStateManager
 *  net.minecraft.class_9848
 */
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.class_9848;
import net.vulkanmod.gl.VkGlFramebuffer;
import net.vulkanmod.render.engine.VkGpuTexture;
import net.vulkanmod.render.engine.VkTextureView;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;

public class VkFbo {
    final int glId = GlStateManager.glGenFramebuffers();
    final VkTextureView colorAttachmentView;
    final VkGpuTexture depthAttachment;

    protected VkFbo(VkTextureView colorAttachmentView, VkGpuTexture depthAttachment) {
        this.colorAttachmentView = colorAttachmentView;
        this.depthAttachment = depthAttachment;
        VkGlFramebuffer fbo = VkGlFramebuffer.getFramebuffer(this.glId);
        VkGpuTexture colorAttachmentTexture = this.colorAttachmentView.texture();
        fbo.setAttachmentTexture(36064, colorAttachmentTexture.id);
        if (depthAttachment != null) {
            fbo.setAttachmentTexture(36096, depthAttachment.id);
        }
        fbo.setLevel(this.colorAttachmentView.baseMipLevel());
    }

    public void bind() {
        VkGlFramebuffer.bindFramebuffer(36160, this.glId);
        this.clearAttachments();
    }

    protected void clearAttachments() {
        int clear = 0;
        VkGpuTexture colorAttachmentTexture = this.colorAttachmentView.texture();
        if (colorAttachmentTexture.needsClear()) {
            clear |= 0x4000;
            int clearColor = colorAttachmentTexture.clearColor;
            VRenderSystem.setClearColor(class_9848.method_65101((int)clearColor), class_9848.method_65102((int)clearColor), class_9848.method_65103((int)clearColor), class_9848.method_65100((int)clearColor));
            colorAttachmentTexture.needsClear = false;
        }
        if (this.depthAttachment != null && this.depthAttachment.needsClear()) {
            clear |= 0x100;
            float clearDepth = this.depthAttachment.depthClearValue;
            VRenderSystem.clearDepth(clearDepth);
            this.depthAttachment.needsClear = false;
        }
        if (clear != 0) {
            Renderer.clearAttachments(clear);
        }
    }

    protected void close() {
        VkGlFramebuffer.deleteFramebuffer(this.glId);
    }

    public boolean needsClear() {
        return this.colorAttachmentView.texture().needsClear() || this.depthAttachment != null && this.depthAttachment.needsClear();
    }
}

