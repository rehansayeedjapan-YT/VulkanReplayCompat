package com.vulkanreplaycompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_310") // MinecraftClient
public class MixinMinecraftClient {

    @Inject(method = "method_1523", at = @At("RETURN"), remap = false)
    private void onFrameEnd(boolean tick, CallbackInfo ci) {
        if (!com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread()) {
            return;
        }
        try {
            Class<?> replayUIClass = Class.forName("com.moulberry.flashback.editor.ui.ReplayUI");
            replayUIClass.getMethod("drawOverlay").invoke(null);
        } catch (Throwable t) {
        }
    }
}
