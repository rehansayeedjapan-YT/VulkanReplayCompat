/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_3678
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package net.vulkanmod.mixin.window;

import net.minecraft.class_1041;
import net.minecraft.class_3678;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_1041.class})
public interface WindowAccessor {
    @Accessor(value="field_5176")
    public class_3678 getEventHandler();
}

