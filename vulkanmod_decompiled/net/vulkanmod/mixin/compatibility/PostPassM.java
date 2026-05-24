/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  net.minecraft.class_283
 *  net.minecraft.class_283$class_9971
 *  net.minecraft.class_9925
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.compatibility;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import java.util.List;
import java.util.Map;
import net.minecraft.class_283;
import net.minecraft.class_9925;
import net.vulkanmod.render.engine.VkGpuTexture;
import net.vulkanmod.vulkan.Renderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_283.class})
public abstract class PostPassM {
    @Shadow
    @Final
    private List<class_283.class_9971> field_53122;

    @Inject(method={"method_67884"}, at={@At(value="INVOKE", target="Lcom/mojang/blaze3d/systems/GpuDevice;createCommandEncoder()Lcom/mojang/blaze3d/systems/CommandEncoder;")})
    private void transitionLayouts(class_9925 resourceHandle, GpuBufferSlice gpuBufferSlice, Map map, CallbackInfo ci) {
        Renderer.getInstance().endRenderPass();
        for (class_283.class_9971 input : this.field_53122) {
            VkGpuTexture gpuTexture = (VkGpuTexture)input.method_71128(map).texture();
            if (gpuTexture.needsClear()) {
                gpuTexture.getFbo(null).bind();
            }
            gpuTexture.getVulkanImage().readOnlyLayout();
        }
    }
}

