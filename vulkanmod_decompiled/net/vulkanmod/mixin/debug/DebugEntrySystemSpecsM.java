/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GLX
 *  com.mojang.blaze3d.systems.GpuDevice
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.class_11627
 *  net.minecraft.class_11630
 *  net.minecraft.class_1937
 *  net.minecraft.class_2818
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.debug;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_11627;
import net.minecraft.class_11630;
import net.minecraft.class_1937;
import net.minecraft.class_2818;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.vulkanmod.Initializer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_11627.class})
public class DebugEntrySystemSpecsM {
    @Shadow
    @Final
    private static class_2960 field_61552;

    @Inject(method={"method_72751"}, at={@At(value="HEAD")}, cancellable=true)
    private void display(class_11630 debugScreenDisplayer, class_1937 level, class_2818 levelChunk, class_2818 levelChunk2, CallbackInfo ci) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        debugScreenDisplayer.method_72744(field_61552, List.of(String.format(Locale.ROOT, "Java: %s", System.getProperty("java.version")), String.format(Locale.ROOT, "CPU: %s", GLX._getCpuInfo()), String.format(Locale.ROOT, "Display: %dx%d (%s)", class_310.method_1551().method_22683().method_4489(), class_310.method_1551().method_22683().method_4506(), gpuDevice.getVendor()), gpuDevice.getRenderer(), String.format(Locale.ROOT, "%s %s", gpuDevice.getBackendName(), gpuDevice.getVersion()), String.format(Locale.ROOT, "VulkanMod %s", Initializer.getVersion())));
        ci.cancel();
    }
}

