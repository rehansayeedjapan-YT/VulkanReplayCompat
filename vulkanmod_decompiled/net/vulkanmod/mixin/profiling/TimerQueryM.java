/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_7168
 *  net.minecraft.class_7168$class_7169
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 */
package net.vulkanmod.mixin.profiling;

import net.minecraft.class_7168;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value={class_7168.class})
public class TimerQueryM {
    @Overwrite
    public void method_41720() {
    }

    @Overwrite
    public class_7168.class_7169 method_41721() {
        return null;
    }
}

