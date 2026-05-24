/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBuffer$MappedView
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.TextureFormat
 *  net.minecraft.class_1011
 *  net.minecraft.class_276
 *  net.minecraft.class_9848
 */
package net.vulkanmod.vulkan.util;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.function.Consumer;
import net.minecraft.class_1011;
import net.minecraft.class_276;
import net.minecraft.class_9848;
import net.vulkanmod.render.engine.VkGpuTexture;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.util.ColorUtil;

public abstract class ScreenshotUtil {
    public static void takeScreenshot(class_276 renderTarget, int mipLevel, Consumer<class_1011> consumer) {
        int width = renderTarget.field_1482;
        int height = renderTarget.field_1481;
        GpuTexture gpuTexture = renderTarget.method_30277();
        if (gpuTexture == null) {
            throw new IllegalStateException("Tried to capture screenshot of an incomplete framebuffer");
        }
        Renderer.getInstance().flushCmds();
        int pixelSize = TextureFormat.RGBA8.pixelSize();
        GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Screenshot buffer", 9, (long)(width * height * pixelSize));
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(gpuTexture, gpuBuffer, 0L, () -> {
            try (GpuBuffer.MappedView readView = commandEncoder.mapBuffer(gpuBuffer, true, false);){
                class_1011 nativeImage = new class_1011(width, height, false);
                VkGpuTexture colorAttachment = (VkGpuTexture)Renderer.getInstance().getMainPass().getColorAttachment();
                boolean isBgraFormat = colorAttachment.getVulkanImage().format == 44;
                int size = mipLevel * mipLevel;
                for (int y = 0; y < height; ++y) {
                    for (int x = 0; x < width; ++x) {
                        if (mipLevel == 1) {
                            int color = readView.data().getInt((x + y * width) * pixelSize);
                            if (isBgraFormat) {
                                color = ColorUtil.BGRAtoRGBA(color);
                            }
                            nativeImage.method_4305(x, y, color | 0xFF000000);
                            continue;
                        }
                        int red = 0;
                        int green = 0;
                        int blue = 0;
                        for (int x1 = 0; x1 < mipLevel; ++x1) {
                            for (int y1 = 0; y1 < mipLevel; ++y1) {
                                int color = readView.data().getInt((x + x1 + (y + y1) * width) * pixelSize);
                                if (isBgraFormat) {
                                    color = ColorUtil.BGRAtoRGBA(color);
                                }
                                red += class_9848.method_61327((int)color);
                                green += class_9848.method_61329((int)color);
                                blue += class_9848.method_61331((int)color);
                            }
                        }
                        nativeImage.method_4305(x, y, class_9848.method_61324((int)255, (int)(red / size), (int)(green / size), (int)(blue / size)));
                    }
                }
                consumer.accept(nativeImage);
            }
            gpuBuffer.close();
        }, 0);
    }
}

