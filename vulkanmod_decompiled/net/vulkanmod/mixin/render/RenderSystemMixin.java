/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.GpuDevice
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.class_11282
 *  net.minecraft.class_12136
 *  net.minecraft.class_12289
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.vulkanmod.mixin.render;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.class_11282;
import net.minecraft.class_12136;
import net.minecraft.class_12289;
import net.vulkanmod.render.engine.VkGpuDevice;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={RenderSystem.class})
public abstract class RenderSystemMixin {
    @Shadow
    @Nullable
    private static Thread renderThread;
    @Shadow
    @Nullable
    private static GpuDevice DEVICE;
    @Shadow
    @Nullable
    private static class_11282 dynamicUniforms;
    @Shadow
    private static class_12136 samplerCache;
    @Shadow
    private static String apiDescription;

    @Overwrite(remap=false)
    public static void initRenderer(long l, int i, boolean bl, class_12289 shaderSource, boolean bl2) {
        renderThread.setPriority(7);
        VRenderSystem.initRenderer();
        DEVICE = new VkGpuDevice(l, i, bl, shaderSource, bl2);
        apiDescription = RenderSystem.getDevice().getImplementationInformation();
        Renderer.initRenderer();
        dynamicUniforms = new class_11282();
        samplerCache.method_75292();
    }
}

