/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1041
 *  net.minecraft.class_155
 *  net.minecraft.class_310
 *  net.minecraft.class_315
 *  net.minecraft.class_3262
 *  net.minecraft.class_3268
 *  net.minecraft.class_8518
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package net.vulkanmod.mixin.wayland;

import java.io.IOException;
import net.minecraft.class_1041;
import net.minecraft.class_155;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_3262;
import net.minecraft.class_3268;
import net.minecraft.class_8518;
import net.vulkanmod.config.Platform;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={class_310.class})
public class MinecraftMixin {
    @Shadow
    @Final
    private class_1041 field_1704;
    @Shadow
    @Final
    public class_315 field_1690;
    @Shadow
    @Final
    private class_3268 field_40380;

    @Redirect(method={"<init>"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_1041;method_4491(Lnet/minecraft/class_3262;Lnet/minecraft/class_8518;)V"))
    private void bypassWaylandIcon(class_1041 instance, class_3262 packResources, class_8518 iconSet) throws IOException {
        if (!Platform.isWayLand()) {
            this.field_1704.method_4491((class_3262)this.field_40380, class_155.method_16673().comp_4031() ? class_8518.field_44650 : class_8518.field_44651);
        }
    }
}

