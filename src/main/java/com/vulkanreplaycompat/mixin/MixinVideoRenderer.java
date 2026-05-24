package com.vulkanreplaycompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.replaymod.render.rendering.VideoRenderer", remap = false)
public class MixinVideoRenderer {

    @Inject(method = "drawGui", at = @At("HEAD"))
    private void onDrawGui(CallbackInfoReturnable<Boolean> cir) {
        // Flush Vulkan commands because drawGui will clear textures which expects no active renderpass
        net.vulkanmod.vulkan.Renderer.getInstance().flushCmds();
    }
}
