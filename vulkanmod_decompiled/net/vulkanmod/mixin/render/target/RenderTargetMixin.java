/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.class_10799
 *  net.minecraft.class_276
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.vulkanmod.mixin.render.target;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.OptionalInt;
import net.minecraft.class_10799;
import net.minecraft.class_276;
import net.vulkanmod.render.engine.VkFbo;
import net.vulkanmod.render.engine.VkGpuTexture;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={class_276.class})
public abstract class RenderTargetMixin {
    @Shadow
    public int field_1482;
    @Shadow
    public int field_1481;
    @Shadow
    @Nullable
    protected GpuTexture field_1475;
    @Shadow
    @Nullable
    protected GpuTexture field_56739;
    @Shadow
    @Nullable
    protected GpuTextureView field_60567;

    @Overwrite
    public void method_68445(GpuTextureView gpuTextureView) {
        RenderSystem.assertOnRenderThread();
        VkFbo fbo = ((VkGpuTexture)this.field_1475).getFbo(this.field_56739);
        if (fbo.needsClear()) {
            return;
        }
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Blit render target", gpuTextureView, OptionalInt.empty());){
            renderPass.setPipeline(class_10799.field_56840);
            RenderSystem.bindDefaultUniforms((RenderPass)renderPass);
            renderPass.bindTexture("InSampler", this.field_60567, RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST));
            renderPass.draw(0, 3);
        }
    }
}

