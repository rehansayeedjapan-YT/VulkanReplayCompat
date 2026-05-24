package com.vulkanreplaycompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.replaymod.render.shader.Program", remap = false)
public class MixinProgram {

    @Inject(method = "<init>", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfo ci) {
        // Prevent ReplayMod from calling OpenGL shader compilation methods like glCreateProgramObjectARB
        // which will crash instantly in VulkanMod due to no active OpenGL context.
        // We cancel the constructor to effectively stub this out.
        ci.cancel();
    }
}
