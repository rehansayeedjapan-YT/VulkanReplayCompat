/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11618
 *  net.minecraft.class_11630
 *  net.minecraft.class_1937
 *  net.minecraft.class_2818
 *  net.minecraft.class_2960
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.vulkanmod.mixin.debug;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_11618;
import net.minecraft.class_11630;
import net.minecraft.class_1937;
import net.minecraft.class_2818;
import net.minecraft.class_2960;
import net.vulkanmod.vulkan.memory.MemoryManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={class_11618.class})
public abstract class DebugEntryMemoryM {
    @Shadow
    @Final
    private static class_2960 field_61547;

    @Shadow
    protected static long method_72758(long l) {
        return 0L;
    }

    @Overwrite
    public void method_72751(class_11630 debugScreenDisplayer, @Nullable class_1937 level, @Nullable class_2818 levelChunk, @Nullable class_2818 levelChunk2) {
        long l = Runtime.getRuntime().maxMemory();
        long m = Runtime.getRuntime().totalMemory();
        long n = Runtime.getRuntime().freeMemory();
        long o = m - n;
        debugScreenDisplayer.method_72744(field_61547, List.of(String.format(Locale.ROOT, "Mem: %2d%% %03d/%03dMB", o * 100L / l, DebugEntryMemoryM.method_72758(o), DebugEntryMemoryM.method_72758(l)), String.format(Locale.ROOT, "Allocated: %2d%% %03dMB", m * 100L / l, DebugEntryMemoryM.method_72758(m)), String.format("Off-heap: " + this.getOffHeapMemory() + "MB", new Object[0]), "NativeMemory: %dMB".formatted(MemoryManager.getInstance().getNativeMemoryMB()), "DeviceMemory: %dMB".formatted(MemoryManager.getInstance().getAllocatedDeviceMemoryMB())));
    }

    private long getOffHeapMemory() {
        return DebugEntryMemoryM.method_72758(ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed());
    }
}

