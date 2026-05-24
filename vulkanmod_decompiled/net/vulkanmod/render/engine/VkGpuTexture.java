/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.opengl.GlStateManager
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.TextureFormat
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceMap
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10868
 *  org.jetbrains.annotations.Nullable
 */
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10868;
import net.vulkanmod.gl.VkGlTexture;
import net.vulkanmod.render.engine.VkFbo;
import net.vulkanmod.render.engine.VkGpuDevice;
import net.vulkanmod.render.engine.VkTextureView;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class VkGpuTexture
extends class_10868 {
    private static final Reference2ReferenceOpenHashMap<class_10868, VkGpuTexture> glToVkMap = new Reference2ReferenceOpenHashMap();
    protected VkGlTexture glTexture;
    protected final int id;
    private final Int2ReferenceMap<VkFbo> fboCache = new Int2ReferenceOpenHashMap();
    protected boolean closed;
    VkTextureView fboView;
    boolean needsClear = false;
    int clearColor = 0;
    float depthClearValue = 1.0f;

    protected VkGpuTexture(int usage, String string, TextureFormat textureFormat, int width, int height, int layers, int mipLevel, int id, VkGlTexture glTexture) {
        super(usage, string, textureFormat, width, height, layers, mipLevel, id);
        this.id = id;
        this.glTexture = glTexture;
    }

    public void close() {
        if (!this.closed) {
            this.closed = true;
            GlStateManager._deleteTexture((int)this.id);
        }
    }

    public boolean isClosed() {
        return this.closed;
    }

    public int method_68427() {
        return this.id;
    }

    public void setClearColor(int clearColor) {
        this.needsClear = true;
        this.clearColor = clearColor;
    }

    public void setDepthClearValue(float depthClearValue) {
        this.needsClear = true;
        this.depthClearValue = depthClearValue;
    }

    public boolean needsClear() {
        return this.needsClear;
    }

    public VkFbo getFbo(@Nullable GpuTexture depthAttachment) {
        int depthAttachmentId;
        int n = depthAttachmentId = depthAttachment == null ? 0 : ((VkGpuTexture)depthAttachment).id;
        if (this.fboView == null) {
            VkGpuDevice gpuDevice = (VkGpuDevice)RenderSystem.getDevice();
            this.fboView = (VkTextureView)gpuDevice.createTextureView((GpuTexture)this, 0, this.getMipLevels());
        }
        return (VkFbo)this.fboCache.computeIfAbsent(depthAttachmentId, j -> new VkFbo(this.fboView, (VkGpuTexture)depthAttachment));
    }

    public VulkanImage getVulkanImage() {
        return this.glTexture.getVulkanImage();
    }

    public static VkGpuTexture fromGlTexture(class_10868 glTexture) {
        return (VkGpuTexture)((Object)glToVkMap.computeIfAbsent((Object)glTexture, glTexture1 -> {
            String name = glTexture.getLabel();
            int id = glTexture.method_68427();
            VkGlTexture vglTexture = VkGlTexture.getTexture(id);
            VkGpuTexture gpuTexture = new VkGpuTexture(0, name, glTexture.getFormat(), glTexture.getWidth(0), glTexture.getHeight(0), 1, glTexture.getMipLevels(), glTexture.method_68427(), vglTexture);
            return gpuTexture;
        }));
    }

    public static TextureFormat textureFormat(int format) {
        return switch (format) {
            case 37, 43, 44 -> TextureFormat.RGBA8;
            case 9 -> TextureFormat.RED8;
            case 126 -> TextureFormat.DEPTH32;
            default -> null;
        };
    }

    public static int vkFormat(TextureFormat textureFormat) {
        return switch (textureFormat) {
            default -> throw new MatchException(null, null);
            case TextureFormat.RGBA8 -> 37;
            case TextureFormat.RED8 -> 9;
            case TextureFormat.RED8I -> 14;
            case TextureFormat.DEPTH32 -> 126;
        };
    }

    public static int vkImageViewType(int usage) {
        int viewType = (usage & 0x10) != 0 ? 3 : 1;
        return viewType;
    }
}

