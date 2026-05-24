package com.vulkanreplaycompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.replaymod.render.capturer.OpenGlFrameCapturer", remap = false)
public class MixinOpenGlFrameCapturer {

    @Inject(method = "captureFrame(ILcom/replaymod/render/capturer/CaptureData;)Lcom/replaymod/render/frame/OpenGlFrame;", at = @At("HEAD"))
    private void onCaptureFrame(int frameId, Object captureData, CallbackInfoReturnable<Object> cir) {
        // Flush Vulkan commands so that the active RenderPass is closed.
        // This allows ReplayMod to call copyTextureToBuffer and mapBuffer without 
        // VulkanMod throwing an "inRenderPass" IllegalStateException.
        net.vulkanmod.vulkan.Renderer.getInstance().flushCmds();
    }
}
