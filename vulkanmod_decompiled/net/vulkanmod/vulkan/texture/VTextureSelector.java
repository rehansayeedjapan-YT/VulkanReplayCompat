/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  org.lwjgl.system.MemoryUtil
 */
package net.vulkanmod.vulkan.texture;

import com.mojang.blaze3d.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.util.List;
import net.vulkanmod.Initializer;
import net.vulkanmod.gl.VkGlTexture;
import net.vulkanmod.render.engine.VkGpuTexture;
import net.vulkanmod.render.texture.SpriteUpdateUtil;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.shader.descriptor.ImageDescriptor;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.lwjgl.system.MemoryUtil;

public abstract class VTextureSelector {
    public static final int SIZE = 12;
    private static final VulkanImage[] boundTextures = new VulkanImage[12];
    private static final int[] levels = new int[12];
    private static final VulkanImage whiteTexture = VulkanImage.createWhiteTexture();
    private static int activeTexture = 0;

    public static void bindTexture(VulkanImage texture) {
        VTextureSelector.boundTextures[0] = texture;
    }

    public static void bindTexture(int i, VulkanImage texture) {
        if (i < 0 || i >= 12) {
            Initializer.LOGGER.error(String.format("On Texture binding: index %d out of range [0, %d]", i, 11));
            return;
        }
        VTextureSelector.boundTextures[i] = texture;
        VTextureSelector.levels[i] = -1;
    }

    public static void bindImage(int i, VulkanImage texture, int level) {
        if (i < 0 || i > 7) {
            Initializer.LOGGER.error(String.format("On Texture binding: index %d out of range [0, %d]", i, 11));
            return;
        }
        VTextureSelector.boundTextures[i] = texture;
        VTextureSelector.levels[i] = level;
    }

    public static void uploadSubTexture(int mipLevel, int width, int height, int xOffset, int yOffset, int unpackSkipRows, int unpackSkipPixels, int unpackRowLength, ByteBuffer buffer) {
        VTextureSelector.uploadSubTexture(mipLevel, 0, width, height, xOffset, yOffset, unpackSkipRows, unpackSkipPixels, unpackRowLength, MemoryUtil.memAddress((ByteBuffer)buffer));
    }

    public static void uploadSubTexture(int mipLevel, int arrayLayer, int width, int height, int xOffset, int yOffset, int unpackSkipRows, int unpackSkipPixels, int unpackRowLength, long bufferPtr) {
        VulkanImage texture = boundTextures[activeTexture];
        if (texture == null) {
            throw new NullPointerException("Texture is null at index: " + activeTexture);
        }
        SpriteUpdateUtil.addTransitionedLayout(texture);
        texture.uploadSubTextureAsync(mipLevel, arrayLayer, width, height, xOffset, yOffset, unpackSkipRows, unpackSkipPixels, unpackRowLength, bufferPtr);
    }

    public static int getTextureIdx(String name) {
        return switch (name) {
            case "Sampler0", "DiffuseSampler", "InSampler", "CloudFaces", "Sprite", "CurrentSprite" -> 0;
            case "Sampler1", "BlurSampler", "NextSprite" -> 1;
            case "Sampler2" -> 2;
            case "Sampler3" -> 3;
            case "Sampler4" -> 4;
            case "Sampler5" -> 5;
            case "Sampler6" -> 6;
            case "Sampler7" -> 7;
            default -> -1;
        };
    }

    public static void bindShaderTextures(Pipeline pipeline) {
        List<ImageDescriptor> imageDescriptors = pipeline.getImageDescriptors();
        for (ImageDescriptor state : imageDescriptors) {
            VkGpuTexture gpuTexture;
            int shaderTexture;
            VkGlTexture texture;
            GpuTextureView textureView = VRenderSystem.getShaderTexture(state.imageIdx);
            if (textureView == null || (texture = VkGlTexture.getTexture(shaderTexture = (gpuTexture = (VkGpuTexture)textureView.texture()).method_68427())) == null || texture.getVulkanImage() == null) continue;
            VTextureSelector.bindTexture(state.imageIdx, texture.getVulkanImage());
        }
    }

    public static VulkanImage getImage(int i) {
        return boundTextures[i];
    }

    public static void setLightTexture(VulkanImage texture) {
        VTextureSelector.boundTextures[2] = texture;
    }

    public static void setOverlayTexture(VulkanImage texture) {
        VTextureSelector.boundTextures[1] = texture;
    }

    public static void setActiveTexture(int activeTexture) {
        if (activeTexture < 0 || activeTexture >= 12) {
            Initializer.LOGGER.error(String.format("On Texture binding: index %d out of range [0, %d]", activeTexture, 11));
        }
        VTextureSelector.activeTexture = activeTexture;
    }

    public static VulkanImage getBoundTexture() {
        return boundTextures[activeTexture];
    }

    public static VulkanImage getBoundTexture(int i) {
        return boundTextures[i];
    }

    public static VulkanImage getWhiteTexture() {
        return whiteTexture;
    }
}

