/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.vulkan.VkCommandBuffer
 */
package net.vulkanmod.vulkan.pass;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

public interface MainPass {
    public void begin(VkCommandBuffer var1, MemoryStack var2);

    public void end(VkCommandBuffer var1);

    public void cleanUp();

    public void onResize();

    default public void mainTargetBindWrite() {
    }

    default public void mainTargetUnbindWrite() {
    }

    default public void rebindMainTarget() {
    }

    default public void bindAsTexture() {
    }

    default public Framebuffer getMainFramebuffer() {
        return null;
    }

    default public GpuTexture getColorAttachment() {
        return null;
    }

    default public GpuTextureView getColorAttachmentView() {
        return null;
    }

    default public GpuTexture getDepthAttachment() {
        return null;
    }
}

