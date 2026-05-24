package com.vulkanreplaycompat.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        try {
            Class<?> replayUIClass = Class.forName("com.moulberry.flashback.editor.ui.ReplayUI");
            com.vulkanreplaycompat.BridgeState.isOurCall = true;
            // System.out.println("[VulkanReplayCompat] Invoking ReplayUI.drawOverlay()");
            replayUIClass.getMethod("drawOverlay").invoke(null);
            com.vulkanreplaycompat.BridgeState.isOurCall = false;
        } catch (Throwable t) {
            System.err.println("[VulkanReplayCompat] Error in InGameHud.render:");
            t.printStackTrace();
            com.vulkanreplaycompat.BridgeState.isOurCall = false;
        }
    }
}
