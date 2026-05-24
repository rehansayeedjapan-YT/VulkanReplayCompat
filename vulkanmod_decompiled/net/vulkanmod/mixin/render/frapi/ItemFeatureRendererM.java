/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10444$class_10445
 *  net.minecraft.class_11687
 *  net.minecraft.class_11788
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_4597$class_4598
 *  net.minecraft.class_4618
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render.frapi;

import net.minecraft.class_10444;
import net.minecraft.class_11687;
import net.minecraft.class_11788;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4618;
import net.vulkanmod.render.chunk.build.frapi.accessor.AccessBatchingRenderCommandQueue;
import net.vulkanmod.render.chunk.build.frapi.render.ItemRenderContext;
import net.vulkanmod.render.chunk.build.frapi.render.MeshItemCommand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_11687.class})
public class ItemFeatureRendererM {
    @Shadow
    @Final
    private class_4587 field_61844;
    @Unique
    private final ItemRenderContext itemRenderContext = new ItemRenderContext();

    @Inject(method={"method_73010"}, at={@At(value="RETURN")})
    private void onReturnRender(class_11788 queue, class_4597.class_4598 vertexConsumers, class_4618 outlineVertexConsumers, CallbackInfo ci) {
        for (MeshItemCommand itemCommand : ((AccessBatchingRenderCommandQueue)queue).getMeshItemCommands()) {
            this.field_61844.method_22903();
            this.field_61844.method_23760().method_66521(itemCommand.positionMatrix());
            this.itemRenderContext.renderModel(itemCommand.displayContext(), this.field_61844, (class_4597)vertexConsumers, itemCommand.lightCoords(), itemCommand.overlayCoords(), itemCommand.tintLayers(), itemCommand.quads(), itemCommand.mesh(), itemCommand.renderLayer(), itemCommand.glintType(), false);
            if (itemCommand.outlineColor() != 0) {
                outlineVertexConsumers.method_23286(itemCommand.outlineColor());
                this.itemRenderContext.renderModel(itemCommand.displayContext(), this.field_61844, (class_4597)outlineVertexConsumers, itemCommand.lightCoords(), itemCommand.overlayCoords(), itemCommand.tintLayers(), itemCommand.quads(), itemCommand.mesh(), itemCommand.renderLayer(), class_10444.class_10445.field_55341, true);
            }
            this.field_61844.method_22909();
        }
    }
}

