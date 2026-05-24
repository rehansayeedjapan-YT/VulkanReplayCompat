/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider
 *  net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer
 *  net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper
 *  net.minecraft.class_1087
 *  net.minecraft.class_1920
 *  net.minecraft.class_2338
 *  net.minecraft.class_2680
 *  net.minecraft.class_4587
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_4597
 *  net.minecraft.class_4608
 *  net.minecraft.class_776
 *  net.minecraft.class_778
 *  net.minecraft.class_9891
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render.frapi;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.minecraft.class_1087;
import net.minecraft.class_1920;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_776;
import net.minecraft.class_778;
import net.minecraft.class_9891;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_776.class})
abstract class BlockRenderDispatcherMixin {
    @Shadow
    @Final
    private class_778 field_4170;

    BlockRenderDispatcherMixin() {
    }

    @Inject(method={"method_23071"}, at={@At(value="INVOKE_ASSIGN", target="Lnet/minecraft/class_773;method_3335(Lnet/minecraft/class_2680;)Lnet/minecraft/class_1087;", shift=At.Shift.AFTER)}, cancellable=true)
    private void afterGetModel(class_2680 blockState, class_2338 blockPos, class_1920 world, class_4587 matrixStack, class_4588 vertexConsumer, CallbackInfo ci, @Local class_1087 model) {
        this.field_4170.render(world, model, blockState, blockPos, matrixStack, layer -> vertexConsumer, true, blockState.method_26190(blockPos), class_4608.field_21444);
        ci.cancel();
    }

    @Redirect(method={"method_3353"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_778;method_3367(Lnet/minecraft/class_4587$class_4665;Lnet/minecraft/class_4588;Lnet/minecraft/class_1087;FFFII)V"))
    private void renderProxy(class_4587.class_4665 entry, class_4588 vertexConsumer, class_1087 model, float red, float green, float blue, int light, int overlay, class_2680 state, class_4587 matrices, class_4597 vertexConsumers, int light1, int overlay1) {
        FabricBlockModelRenderer.render((class_4587.class_4665)entry, (BlockVertexConsumerProvider)RenderLayerHelper.entityDelegate((class_4597)vertexConsumers), (class_1087)model, (float)red, (float)green, (float)blue, (int)light, (int)overlay, (class_1920)class_9891.field_52611, (class_2338)class_2338.field_10980, (class_2680)state);
    }
}

