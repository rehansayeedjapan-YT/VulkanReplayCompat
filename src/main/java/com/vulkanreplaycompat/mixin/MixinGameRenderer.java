package com.vulkanreplaycompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_757") // GameRenderer
public class MixinGameRenderer {

    // method_31904 is render(FJZ)V (float tickDelta, long startTime, boolean tick)
    @Inject(method = "render", at = @At("RETURN"))
    private void afterGameRender(net.minecraft.client.render.RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (!com.mojang.blaze3d.systems.RenderSystem.isOnRenderThread()) {
            return;
        }
        try {
            Class<?> replayUIClass = Class.forName("com.moulberry.flashback.editor.ui.ReplayUI");
            replayUIClass.getMethod("drawOverlay").invoke(null);
            
            // Note: because we called drawOverlay here, Flashback's own invocation of drawOverlay
            // inside 'afterMainBlit' will effectively do nothing, because the ImGui frame data
            // will have already been consumed and processed!
        } catch (Throwable t) {
            // It's possible Flashback isn't loaded or method changed, ignore safely
        }
    }
}
