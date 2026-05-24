/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  com.mojang.blaze3d.pipeline.CompiledRenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.systems.GpuDevice
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.class_10151
 *  net.minecraft.class_10151$class_10153
 *  net.minecraft.class_10151$class_10170
 *  net.minecraft.class_3300
 *  net.minecraft.class_3695
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render.shader;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.class_10151;
import net.minecraft.class_3300;
import net.minecraft.class_3695;
import net.vulkanmod.render.shader.CustomRenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_10151.class})
public class ShaderManagerM {
    @Inject(method={"method_62945(Lnet/minecraft/class_10151$class_10153;Lnet/minecraft/class_3300;Lnet/minecraft/class_3695;)V"}, at={@At(value="INVOKE", target="Ljava/util/List;isEmpty()Z")})
    private void onApply(class_10151.class_10153 configs, class_3300 resourceManager, class_3695 profilerFiller, CallbackInfo ci, @Local class_10151.class_10170 compilationCache) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        List<RenderPipeline> pipelines = CustomRenderPipelines.pipelines;
        for (RenderPipeline renderPipeline : pipelines) {
            CompiledRenderPipeline compiledRenderPipeline = gpuDevice.precompilePipeline(renderPipeline, (arg_0, arg_1) -> ((class_10151.class_10170)compilationCache).method_68498(arg_0, arg_1));
        }
    }
}

