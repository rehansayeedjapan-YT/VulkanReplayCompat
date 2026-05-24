/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_3300
 *  net.minecraft.class_4063
 *  net.minecraft.class_638
 *  net.minecraft.class_761
 *  net.minecraft.class_9909
 *  net.minecraft.class_9916
 *  net.minecraft.class_9960
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render.clouds;

import net.minecraft.class_243;
import net.minecraft.class_3300;
import net.minecraft.class_4063;
import net.minecraft.class_638;
import net.minecraft.class_761;
import net.minecraft.class_9909;
import net.minecraft.class_9916;
import net.minecraft.class_9960;
import net.vulkanmod.render.profiling.Profiler;
import net.vulkanmod.render.sky.CloudRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_761.class})
public abstract class LevelRendererM {
    @Shadow
    private int field_4073;
    @Shadow
    @Nullable
    private class_638 field_4085;
    @Shadow
    @Final
    private class_9960 field_53081;
    @Unique
    private CloudRenderer vmCloudRenderer;

    @Inject(method={"method_62204"}, at={@At(value="HEAD")}, cancellable=true)
    public void addCloudsPass(class_9909 frameGraphBuilder, class_4063 cloudStatus, class_243 camPos, long gameTime, float partialTicks, int cloudColor, float cloudHeight, CallbackInfo ci) {
        if (this.vmCloudRenderer == null) {
            this.vmCloudRenderer = new CloudRenderer();
        }
        class_9916 framePass = frameGraphBuilder.method_61911("clouds");
        if (this.field_53081.field_53096 != null) {
            this.field_53081.field_53096 = framePass.method_61933(this.field_53081.field_53096);
        } else {
            this.field_53081.field_53091 = framePass.method_61933(this.field_53081.field_53091);
        }
        framePass.method_61929(() -> {
            Profiler profiler = Profiler.getMainProfiler();
            profiler.push("Clouds");
            this.vmCloudRenderer.renderClouds(cloudHeight, cloudColor, camPos.method_10216(), camPos.method_10214(), camPos.method_10215(), gameTime, partialTicks);
            profiler.pop();
        });
        ci.cancel();
    }

    @Inject(method={"method_3279"}, at={@At(value="RETURN")})
    private void onAllChanged(CallbackInfo ci) {
        if (this.vmCloudRenderer != null) {
            this.vmCloudRenderer.resetBuffer();
        }
    }

    @Inject(method={"method_14491"}, at={@At(value="RETURN")})
    private void onReload(class_3300 resourceManager, CallbackInfo ci) {
        if (this.vmCloudRenderer != null) {
            this.vmCloudRenderer.loadTexture();
        }
    }
}

