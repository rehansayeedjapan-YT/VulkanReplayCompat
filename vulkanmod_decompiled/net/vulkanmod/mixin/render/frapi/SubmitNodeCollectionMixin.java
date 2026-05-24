/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.renderer.v1.mesh.MeshView
 *  net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter
 *  net.minecraft.class_10444$class_10445
 *  net.minecraft.class_11785
 *  net.minecraft.class_11788
 *  net.minecraft.class_1921
 *  net.minecraft.class_4587
 *  net.minecraft.class_777
 *  net.minecraft.class_811
 *  org.jspecify.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render.frapi;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter;
import net.minecraft.class_10444;
import net.minecraft.class_11785;
import net.minecraft.class_11788;
import net.minecraft.class_1921;
import net.minecraft.class_4587;
import net.minecraft.class_777;
import net.minecraft.class_811;
import net.vulkanmod.render.chunk.build.frapi.accessor.AccessBatchingRenderCommandQueue;
import net.vulkanmod.render.chunk.build.frapi.accessor.AccessRenderCommandQueue;
import net.vulkanmod.render.chunk.build.frapi.render.MeshItemCommand;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_11788.class})
abstract class SubmitNodeCollectionMixin
implements class_11785,
AccessRenderCommandQueue,
AccessBatchingRenderCommandQueue {
    @Shadow
    private boolean field_62243;
    @Unique
    private final List<MeshItemCommand> meshItemCommands = new ArrayList<MeshItemCommand>();

    SubmitNodeCollectionMixin() {
    }

    @Inject(method={"method_73527()V"}, at={@At(value="RETURN")})
    public void clear(CallbackInfo ci) {
        this.meshItemCommands.clear();
    }

    @Override
    public void submitItem(class_4587 matrices, class_811 displayContext, int light, int overlay, int outlineColors, int[] tintLayers, List<class_777> quads, class_1921 renderLayer, class_10444.class_10445 glintType, MeshView mesh, @Nullable ItemRenderTypeGetter renderTypeGetter) {
        this.field_62243 = true;
        this.meshItemCommands.add(new MeshItemCommand(matrices.method_23760().method_56822(), displayContext, light, overlay, outlineColors, tintLayers, quads, renderLayer, glintType, mesh, renderTypeGetter));
    }

    @Override
    public List<MeshItemCommand> getMeshItemCommands() {
        return this.meshItemCommands;
    }
}

