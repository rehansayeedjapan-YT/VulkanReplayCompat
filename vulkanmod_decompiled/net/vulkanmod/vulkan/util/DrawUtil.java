/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.vulkan.VK11
 *  org.lwjgl.vulkan.VkCommandBuffer
 */
package net.vulkanmod.vulkan.util;

import net.vulkanmod.render.PipelineManager;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkCommandBuffer;

public class DrawUtil {
    public static void blitToScreen() {
        DrawUtil.fastBlit();
    }

    public static void fastBlit() {
        GraphicsPipeline blitPipeline = PipelineManager.getFastBlitPipeline();
        VRenderSystem.disableCull();
        VRenderSystem.setPrimitiveTopologyGL(4);
        Renderer renderer = Renderer.getInstance();
        renderer.bindGraphicsPipeline(blitPipeline);
        renderer.uploadAndBindUBOs(blitPipeline);
        VkCommandBuffer commandBuffer = Renderer.getCommandBuffer();
        VK11.vkCmdDraw((VkCommandBuffer)commandBuffer, (int)3, (int)1, (int)0, (int)0);
        VRenderSystem.enableCull();
    }
}

