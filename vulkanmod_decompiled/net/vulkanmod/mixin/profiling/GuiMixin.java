/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_340
 *  net.minecraft.class_9779
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.profiling;

import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_340;
import net.minecraft.class_9779;
import net.vulkanmod.render.profiling.ProfilerOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_329.class})
public class GuiMixin {
    @Shadow
    @Final
    private class_340 field_2026;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void createProfilerOverlay(class_310 minecraft, CallbackInfo ci) {
        ProfilerOverlay.createInstance(minecraft);
    }

    @Inject(method={"method_1753"}, at={@At(value="RETURN")})
    private void renderProfilerOverlay(class_332 guiGraphics, class_9779 deltaTracker, CallbackInfo ci) {
        if (ProfilerOverlay.shouldRender && !this.field_2026.method_53536()) {
            ProfilerOverlay.INSTANCE.render(guiGraphics);
        }
    }
}

