/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.renderer.v1.render.FabricLayerRenderState
 *  net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter
 *  net.minecraft.class_10444$class_10445
 *  net.minecraft.class_10444$class_10446
 *  net.minecraft.class_11659
 *  net.minecraft.class_1921
 *  net.minecraft.class_4587
 *  net.minecraft.class_777
 *  net.minecraft.class_811
 *  org.jspecify.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render.frapi;

import java.util.List;
import net.fabricmc.fabric.api.renderer.v1.render.FabricLayerRenderState;
import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter;
import net.minecraft.class_10444;
import net.minecraft.class_11659;
import net.minecraft.class_1921;
import net.minecraft.class_4587;
import net.minecraft.class_777;
import net.minecraft.class_811;
import net.vulkanmod.render.chunk.build.frapi.accessor.AccessLayerRenderState;
import net.vulkanmod.render.chunk.build.frapi.accessor.AccessRenderCommandQueue;
import net.vulkanmod.render.chunk.build.frapi.mesh.MutableMeshImpl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_10444.class_10446.class})
abstract class ItemStackRenderStateLayerRenderStateM
implements FabricLayerRenderState,
AccessLayerRenderState {
    @Unique
    private final MutableMeshImpl mutableMesh = new MutableMeshImpl();
    @Unique
    private @Nullable ItemRenderTypeGetter renderTypeGetter = null;

    ItemStackRenderStateLayerRenderStateM() {
    }

    @Inject(method={"method_65612()V"}, at={@At(value="RETURN")})
    private void onReturnClear(CallbackInfo ci) {
        this.mutableMesh.clear();
        this.renderTypeGetter = null;
    }

    @Redirect(method={"method_65614"}, at=@At(value="INVOKE", target="Lnet/minecraft/class_11659;method_73480(Lnet/minecraft/class_4587;Lnet/minecraft/class_811;III[ILjava/util/List;Lnet/minecraft/class_1921;Lnet/minecraft/class_10444$class_10445;)V"))
    private void submitItemProxy(class_11659 commandQueue, class_4587 matrices, class_811 displayContext, int light, int overlay, int outlineColor, int[] tints, List<class_777> quads, class_1921 layer, class_10444.class_10445 glint) {
        if (this.mutableMesh.size() > 0 && commandQueue instanceof AccessRenderCommandQueue) {
            AccessRenderCommandQueue access = (AccessRenderCommandQueue)commandQueue;
            access.submitItem(matrices, displayContext, light, overlay, outlineColor, tints, quads, layer, glint, this.mutableMesh, this.renderTypeGetter);
        } else {
            commandQueue.method_73480(matrices, displayContext, light, overlay, outlineColor, tints, quads, layer, glint);
        }
    }

    @Override
    public MutableMeshImpl getMutableMesh() {
        return this.mutableMesh;
    }

    @Override
    public void setRenderTypeGetter(ItemRenderTypeGetter renderTypeGetter) {
        this.renderTypeGetter = renderTypeGetter;
    }
}

