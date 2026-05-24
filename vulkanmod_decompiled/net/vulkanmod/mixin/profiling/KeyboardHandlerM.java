/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_11908
 *  net.minecraft.class_309
 *  net.minecraft.class_310
 *  net.minecraft.class_3675
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.profiling;

import net.minecraft.class_1041;
import net.minecraft.class_11908;
import net.minecraft.class_309;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.vulkanmod.render.profiling.BuildTimeProfiler;
import net.vulkanmod.render.profiling.ProfilerOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_309.class})
public class KeyboardHandlerM {
    @Inject(method={"method_1466"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_304;method_1416(Lnet/minecraft/class_3675$class_306;Z)V", ordinal=1, shift=At.Shift.AFTER)})
    private void injOverlayToggle(long l, int i, class_11908 keyEvent, CallbackInfo ci) {
        if (class_3675.method_15987((class_1041)class_310.method_1551().method_22683(), (int)342)) {
            switch (keyEvent.comp_4795()) {
                case 297: {
                    ProfilerOverlay.toggle();
                    break;
                }
                case 299: {
                    BuildTimeProfiler.startBench();
                }
            }
        } else if (ProfilerOverlay.shouldRender) {
            ProfilerOverlay.onKeyPress(keyEvent.comp_4795());
        }
    }
}

