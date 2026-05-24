/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_11908
 *  net.minecraft.class_309
 *  net.minecraft.class_310
 *  net.minecraft.class_3675
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.debug;

import net.minecraft.class_1041;
import net.minecraft.class_11908;
import net.minecraft.class_309;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_309.class})
public abstract class KeyboardHandlerM {
    @Shadow
    private boolean field_1679;

    @Shadow
    protected abstract boolean method_35696(class_11908 var1);

    @Inject(method={"method_1466"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_304;method_1416(Lnet/minecraft/class_3675$class_306;Z)V", ordinal=1)})
    private void chunkDebug(long l, int i, class_11908 keyEvent, CallbackInfo ci) {
        this.field_1679 |= class_3675.method_15987((class_1041)class_310.method_1551().method_22683(), (int)296) && this.method_35696(keyEvent);
    }
}

