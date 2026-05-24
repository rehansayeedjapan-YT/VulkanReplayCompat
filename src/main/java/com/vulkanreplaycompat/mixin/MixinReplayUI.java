package com.vulkanreplaycompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.moulberry.flashback.editor.ui.ReplayUI", remap = false)
public class MixinReplayUI {

    @Inject(method = "drawOverlay", at = @At("HEAD"), cancellable = true)
    private static void onDrawOverlay(CallbackInfo ci) {
        if (!com.vulkanreplaycompat.BridgeState.isOurCall) {
            // Cancel Flashback's own call which happens at afterMainBlit
            // because VulkanMod has no active RenderPass at that point
            ci.cancel();
        }
    }
}
