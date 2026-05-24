package com.vulkanreplaycompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Map;

@Mixin(targets = "com.replaymod.render.capturer.PboOpenGlFrameCapturer", remap = false)
public class MixinPboOpenGlFrameCapturer {

    @Inject(method = "captureFrame", at = @At("HEAD"))
    private void onCaptureFrame(int frameId, Object captureData, CallbackInfoReturnable<Object> cir) {
        // Flush Vulkan commands to close the active RenderPass
        // so ReplayMod's mapBuffer and copyTextureToBuffer work cleanly.
        net.vulkanmod.vulkan.Renderer.getInstance().flushCmds();
    }
    
    @Inject(method = "process", at = @At("HEAD"))
    private void onProcess(CallbackInfoReturnable<Map<Object, Object>> cir) {
        // We also need to flush commands before process() reads from the mapped buffer,
        // just in case there's an active RenderPass that needs resolving.
        net.vulkanmod.vulkan.Renderer.getInstance().flushCmds();
    }
}
