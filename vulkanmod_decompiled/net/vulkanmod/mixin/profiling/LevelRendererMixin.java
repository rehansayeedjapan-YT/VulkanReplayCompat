/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  net.minecraft.class_11658
 *  net.minecraft.class_243
 *  net.minecraft.class_3695
 *  net.minecraft.class_4063
 *  net.minecraft.class_761
 *  net.minecraft.class_9925
 *  org.joml.Matrix4f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.profiling;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.class_11658;
import net.minecraft.class_243;
import net.minecraft.class_3695;
import net.minecraft.class_4063;
import net.minecraft.class_761;
import net.minecraft.class_9925;
import net.vulkanmod.render.profiling.Profiler;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_761.class})
public class LevelRendererMixin {
    @Inject(method={"method_62205"}, at={@At(value="HEAD")})
    private void pushProfiler(int i, class_4063 cloudStatus, float f, class_243 vec3, long l, float g, CallbackInfo ci) {
        Profiler profiler = Profiler.getMainProfiler();
        profiler.push("Clouds");
    }

    @Inject(method={"method_62205"}, at={@At(value="RETURN")})
    private void popProfiler(int i, class_4063 cloudStatus, float f, class_243 vec3, long l, float g, CallbackInfo ci) {
        Profiler profiler = Profiler.getMainProfiler();
        profiler.pop();
    }

    @Inject(method={"method_62213"}, at={@At(value="HEAD")})
    private void pushProfiler3(GpuBufferSlice gpuBufferSlice, class_9925 resourceHandle, class_9925 resourceHandle2, CallbackInfo ci) {
        Profiler profiler = Profiler.getMainProfiler();
        profiler.push("Particles");
    }

    @Inject(method={"method_62213"}, at={@At(value="RETURN")})
    private void popProfiler3(GpuBufferSlice gpuBufferSlice, class_9925 resourceHandle, class_9925 resourceHandle2, CallbackInfo ci) {
        Profiler profiler = Profiler.getMainProfiler();
        profiler.pop();
    }

    @Inject(method={"method_62214"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_761;method_72916(Lnet/minecraft/class_4587;Lnet/minecraft/class_11658;Lnet/minecraft/class_11659;)V")})
    private void profilerTerrain2(GpuBufferSlice gpuBufferSlice, class_11658 levelRenderState, class_3695 profilerFiller, Matrix4f matrix4f, class_9925 resourceHandle, class_9925 resourceHandle2, boolean bl, class_9925 resourceHandle3, class_9925 resourceHandle4, CallbackInfo ci) {
        Profiler profiler = Profiler.getMainProfiler();
        profiler.pop();
        profiler.push("entities");
    }
}

