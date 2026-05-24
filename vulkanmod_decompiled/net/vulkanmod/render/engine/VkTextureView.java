/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceMap
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
 *  org.jetbrains.annotations.Nullable
 */
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.vulkanmod.render.engine.VkFbo;
import net.vulkanmod.render.engine.VkGpuTexture;
import org.jetbrains.annotations.Nullable;

public class VkTextureView
extends GpuTextureView {
    private boolean closed;
    private final Int2ReferenceMap<VkFbo> fboCache = new Int2ReferenceOpenHashMap();

    protected VkTextureView(VkGpuTexture gpuTexture, int baseMipLevel, int mipLevels) {
        super((GpuTexture)gpuTexture, baseMipLevel, mipLevels);
        gpuTexture.method_71635();
    }

    public VkFbo getFbo(@Nullable GpuTexture depthAttachment) {
        int depthAttachmentId = depthAttachment == null ? 0 : ((VkGpuTexture)depthAttachment).id;
        return (VkFbo)this.fboCache.computeIfAbsent(depthAttachmentId, j -> new VkFbo(this, (VkGpuTexture)depthAttachment));
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.texture().method_71636();
        }
        for (VkFbo fbo : this.fboCache.values()) {
            fbo.close();
        }
    }

    public VkGpuTexture texture() {
        return (VkGpuTexture)super.texture();
    }
}

