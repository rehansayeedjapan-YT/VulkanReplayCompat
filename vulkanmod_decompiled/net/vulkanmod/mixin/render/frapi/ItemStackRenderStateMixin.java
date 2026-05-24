/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.sugar.Local
 *  com.llamalad7.mixinextras.sugar.Share
 *  com.llamalad7.mixinextras.sugar.ref.LocalRef
 *  net.minecraft.class_10444
 *  net.minecraft.class_10444$class_10446
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render.frapi;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import java.util.function.Consumer;
import net.minecraft.class_10444;
import net.vulkanmod.render.chunk.build.frapi.accessor.AccessLayerRenderState;
import net.vulkanmod.render.chunk.build.frapi.mesh.MutableMeshImpl;
import net.vulkanmod.render.chunk.build.frapi.render.QuadToPosPipe;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_10444.class})
abstract class ItemStackRenderStateMixin {
    ItemStackRenderStateMixin() {
    }

    @Inject(method={"method_67991"}, at={@At(value="NEW", target="()Lnet/minecraft/class_4587$class_4665;")})
    private void afterInitVecLoad(Consumer<Vector3fc> posConsumer, CallbackInfo ci, @Local Vector3f vec, @Share(value="pipe") LocalRef<QuadToPosPipe> pipeRef) {
        pipeRef.set((Object)new QuadToPosPipe(posConsumer, vec));
    }

    @Inject(method={"method_67991"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_4587$class_4665;method_67801()V", shift=At.Shift.BEFORE)})
    private void afterLayerLoad(Consumer<Vector3fc> posConsumer, CallbackInfo ci, @Local(ordinal=0) Vector3f vec, @Local class_10444.class_10446 layer, @Local Matrix4f matrix, @Share(value="pipe") LocalRef<QuadToPosPipe> pipeRef) {
        MutableMeshImpl mutableMesh = ((AccessLayerRenderState)layer).getMutableMesh();
        if (mutableMesh.size() > 0) {
            QuadToPosPipe pipe = (QuadToPosPipe)pipeRef.get();
            pipe.matrix = matrix;
            mutableMesh.forEachMutable(pipe);
        }
    }
}

