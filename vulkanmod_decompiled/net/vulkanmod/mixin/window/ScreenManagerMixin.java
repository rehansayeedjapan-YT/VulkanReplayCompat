/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  net.minecraft.class_313
 *  net.minecraft.class_323
 *  net.minecraft.class_3676
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.window;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.class_313;
import net.minecraft.class_323;
import net.minecraft.class_3676;
import net.vulkanmod.config.video.VideoModeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_323.class})
public class ScreenManagerMixin {
    @Shadow
    @Final
    private Long2ObjectMap<class_313> field_1993;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void getMonitors(class_3676 monitorCreator, CallbackInfo ci) {
        VideoModeManager.init(this.field_1993);
    }

    @Inject(method={"method_1683"}, at={@At(value="RETURN")})
    private void onMonitorChange(long monitor, int event, CallbackInfo ci) {
        if (event == 262145) {
            VideoModeManager.addMonitorVideoModes(monitor);
        } else if (event == 262146) {
            VideoModeManager.removeMonitor(monitor);
        }
    }
}

