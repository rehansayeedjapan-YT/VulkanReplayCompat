/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_6396
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.debug.crash_report;

import net.minecraft.class_6396;
import net.vulkanmod.vulkan.device.DeviceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_6396.class})
public class SystemReportM {
    @Inject(method={"method_37124"}, at={@At(value="RETURN")})
    private void addVulkanDevicesInfo(StringBuilder stringBuilder, CallbackInfo ci) {
        stringBuilder.append("\n\n -- VulkanMod Device Report --");
        stringBuilder.append(DeviceManager.getAvailableDevicesInfo());
    }
}

