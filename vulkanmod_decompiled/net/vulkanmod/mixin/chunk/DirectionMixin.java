/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2350
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.vulkanmod.mixin.chunk;

import net.minecraft.class_2350;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={class_2350.class})
public class DirectionMixin {
    @Shadow
    @Final
    private static class_2350[] field_11038;
    @Shadow
    @Final
    private int field_11031;

    @Overwrite
    public class_2350 method_10153() {
        return field_11038[this.field_11031];
    }
}

