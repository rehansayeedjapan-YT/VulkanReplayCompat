/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_315
 *  net.minecraft.class_429
 *  net.minecraft.class_437
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.vulkanmod.mixin.screen;

import net.minecraft.class_2561;
import net.minecraft.class_315;
import net.minecraft.class_429;
import net.minecraft.class_437;
import net.vulkanmod.config.gui.VOptionScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={class_429.class})
public class OptionsScreenM
extends class_437 {
    @Shadow
    @Final
    private class_437 field_2501;
    @Shadow
    @Final
    private class_315 field_2502;

    protected OptionsScreenM(class_2561 title) {
        super(title);
    }

    @Inject(method={"method_19828"}, at={@At(value="HEAD")}, cancellable=true)
    private void injectVideoOptionScreen(CallbackInfoReturnable<class_437> cir) {
        cir.setReturnValue((Object)new VOptionScreen((class_2561)class_2561.method_43470((String)"Video Setting"), this));
    }
}

