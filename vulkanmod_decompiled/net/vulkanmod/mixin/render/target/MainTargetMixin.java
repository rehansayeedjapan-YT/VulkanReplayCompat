/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.GpuDevice
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.textures.TextureFormat
 *  net.minecraft.class_276
 *  net.minecraft.class_6364
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 */
package net.vulkanmod.mixin.render.target;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.class_276;
import net.minecraft.class_6364;
import net.vulkanmod.vulkan.Renderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value={class_6364.class})
public class MainTargetMixin
extends class_276 {
    public MainTargetMixin(boolean useDepth) {
        super("Main", useDepth);
    }

    @Overwrite
    private void method_36802(int width, int height) {
        this.field_1482 = width;
        this.field_1481 = height;
    }

    public void method_1231(int i, int j) {
        RenderSystem.assertOnRenderThread();
        GpuDevice gpuDevice = RenderSystem.getDevice();
        int k = gpuDevice.getMaxTextureSize();
        if (i > 0 && i <= k && j > 0 && j <= k) {
            this.field_1482 = i;
            this.field_1481 = j;
            if (this.field_1478) {
                this.field_56739 = gpuDevice.createTexture(() -> this.field_56738 + " / Depth", 15, TextureFormat.DEPTH32, i, j, 1, 1);
                this.field_60568 = gpuDevice.createTextureView(this.field_56739);
            }
        } else {
            throw new IllegalArgumentException("Window " + i + "x" + j + " size out of bounds (max. size: " + k + ")");
        }
        this.field_1475 = gpuDevice.createTexture(() -> this.field_56738 + " / Color", 15, TextureFormat.RGBA8, i, j, 1, 1);
        this.field_60567 = gpuDevice.createTextureView(this.field_1475);
    }

    public GpuTexture method_30277() {
        return Renderer.getInstance().getMainPass().getColorAttachment();
    }

    public GpuTextureView method_71639() {
        return Renderer.getInstance().getMainPass().getColorAttachmentView();
    }

    public GpuTexture method_30278() {
        return Renderer.getInstance().getMainPass().getDepthAttachment();
    }
}

