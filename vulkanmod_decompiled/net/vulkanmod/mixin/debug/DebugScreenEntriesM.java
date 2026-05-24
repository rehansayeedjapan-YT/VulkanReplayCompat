/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11631
 *  net.minecraft.class_11632
 *  net.minecraft.class_2960
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.debug;

import net.minecraft.class_11631;
import net.minecraft.class_11632;
import net.minecraft.class_2960;
import net.vulkanmod.render.profiling.DebugEntryMemoryStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_11631.class})
public abstract class DebugScreenEntriesM {
    @Shadow
    public static class_2960 method_72763(class_2960 resourceLocation, class_11632 debugScreenEntry) {
        return null;
    }

    @Inject(method={"<clinit>"}, at={@At(value="RETURN")})
    private static void addEntry(CallbackInfo ci) {
        DebugScreenEntriesM.method_72763(class_2960.method_60655((String)"vkmod", (String)"stats"), new DebugEntryMemoryStats());
    }
}

