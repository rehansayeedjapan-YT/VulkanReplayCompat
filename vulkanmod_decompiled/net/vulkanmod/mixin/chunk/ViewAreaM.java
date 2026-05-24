/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_769
 *  net.minecraft.class_846
 *  net.minecraft.class_846$class_851
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.chunk;

import net.minecraft.class_769;
import net.minecraft.class_846;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_769.class})
public abstract class ViewAreaM {
    @Shadow
    public class_846.class_851[] field_4150;

    @Shadow
    protected abstract void method_3325(int var1);

    @Inject(method={"method_3324"}, at={@At(value="HEAD")})
    private void skipAllocation(class_846 sectionRenderDispatcher, CallbackInfo ci) {
        this.method_3325(0);
    }
}

