/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_324
 *  net.minecraft.class_776
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package net.vulkanmod.mixin.render.frapi;

import net.minecraft.class_324;
import net.minecraft.class_776;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={class_776.class})
public interface BlockRenderDispatcherAccessor {
    @Accessor(value="field_20987")
    public class_324 getBlockColors();
}

