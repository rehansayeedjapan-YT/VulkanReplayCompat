package com.vulkanreplaycompat.mixin;

import net.vulkanmod.vulkan.Renderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.moulberry.flashback.exporting.SaveableFramebuffer", remap = false)
public class MixinSaveableFramebuffer {

    @Inject(method = "startDownload", at = @At("HEAD"))
    public void flushBeforeCopy(int texture, int width, int height, CallbackInfo ci) {
        Renderer.getInstance().flushCmds();
    }
}
