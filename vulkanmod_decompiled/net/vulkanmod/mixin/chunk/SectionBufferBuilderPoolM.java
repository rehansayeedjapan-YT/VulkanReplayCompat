/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_8901
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package net.vulkanmod.mixin.chunk;

import net.minecraft.class_8901;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={class_8901.class})
public class SectionBufferBuilderPoolM {
    @ModifyVariable(method={"method_54643"}, at=@At(value="STORE"), ordinal=1)
    private static int skipAllocation(int value) {
        return 0;
    }
}

