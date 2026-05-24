/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.renderer.v1.mesh.MeshView
 *  net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter
 *  net.minecraft.class_10444$class_10445
 *  net.minecraft.class_11659
 *  net.minecraft.class_11661
 *  net.minecraft.class_11785
 *  net.minecraft.class_1921
 *  net.minecraft.class_4587
 *  net.minecraft.class_777
 *  net.minecraft.class_811
 *  org.spongepowered.asm.mixin.Mixin
 */
package net.vulkanmod.mixin.render.frapi;

import java.util.List;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter;
import net.minecraft.class_10444;
import net.minecraft.class_11659;
import net.minecraft.class_11661;
import net.minecraft.class_11785;
import net.minecraft.class_1921;
import net.minecraft.class_4587;
import net.minecraft.class_777;
import net.minecraft.class_811;
import net.vulkanmod.render.chunk.build.frapi.accessor.AccessRenderCommandQueue;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={class_11661.class})
abstract class SubmitNodeStorageMixin
implements class_11659,
AccessRenderCommandQueue {
    SubmitNodeStorageMixin() {
    }

    @Override
    public void submitItem(class_4587 matrices, class_811 displayContext, int light, int overlay, int outlineColors, int[] tintLayers, List<class_777> quads, class_1921 renderLayer, class_10444.class_10445 glintType, MeshView mesh, ItemRenderTypeGetter renderTypeGetter) {
        class_11785 queue = this.method_73529(0);
        if (queue instanceof AccessRenderCommandQueue) {
            AccessRenderCommandQueue access = (AccessRenderCommandQueue)queue;
            access.submitItem(matrices, displayContext, light, overlay, outlineColors, tintLayers, quads, renderLayer, glintType, mesh, renderTypeGetter);
        } else {
            queue.method_73480(matrices, displayContext, light, overlay, outlineColors, tintLayers, quads, renderLayer, glintType);
        }
    }
}

