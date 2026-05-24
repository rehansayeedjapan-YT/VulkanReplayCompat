/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2350
 *  net.minecraft.class_4583
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.vertex;

import net.minecraft.class_2350;
import net.minecraft.class_4583;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.vulkanmod.interfaces.ExtendedVertexBuilder;
import net.vulkanmod.render.vertex.format.I32_SNorm;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class VertexMultiConsumersM {

    @Mixin(value={class_4583.class})
    public static abstract class SheetDecalM
    implements ExtendedVertexBuilder {
        @Shadow
        @Final
        private class_4588 field_20866;
        @Shadow
        @Final
        private Matrix3f field_21054;
        @Shadow
        @Final
        private Matrix4f field_21053;
        @Shadow
        @Final
        private float field_41091;
        @Unique
        private boolean canUseFastVertex = false;
        private Vector3f normal = new Vector3f();
        private Vector4f position = new Vector4f();

        @Override
        public boolean canUseFastVertex() {
            return this.canUseFastVertex;
        }

        @Inject(method={"<init>"}, at={@At(value="RETURN")})
        private void checkDelegates(class_4588 vertexConsumer, class_4587.class_4665 pose, float f, CallbackInfo ci) {
            this.canUseFastVertex = ExtendedVertexBuilder.of(this.field_20866) != null;
        }

        @Override
        public void vertex(float x, float y, float z, int packedColor, float u, float v, int overlay, int light, int packedNormal) {
            float nx = I32_SNorm.unpackX(packedNormal);
            float ny = I32_SNorm.unpackY(packedNormal);
            float nz = I32_SNorm.unpackZ(packedNormal);
            this.normal.set(nx, ny, nz);
            this.position.set(x, y, z, 1.0f);
            this.field_21054.transform(this.normal);
            class_2350 direction = class_2350.method_10147((float)this.normal.x(), (float)this.normal.y(), (float)this.normal.z());
            this.field_21053.transform(this.position);
            this.position.rotateY((float)Math.PI);
            this.position.rotateX(-1.5707964f);
            this.position.rotate((Quaternionfc)direction.method_23224());
            float f = -this.position.x() * this.field_41091;
            float g = -this.position.y() * this.field_41091;
            int color = -1;
            this.field_20866.method_23919(x, y, z, -1, f, g, overlay, light, nx, ny, nz);
        }
    }

    @Mixin(targets={"net/minecraft/class_4720$class_6189"})
    public static class MultipleM
    implements ExtendedVertexBuilder {
        @Shadow
        @Final
        private class_4588[] comp_2847;
        @Unique
        private boolean canUseFastVertex = false;

        @Override
        public boolean canUseFastVertex() {
            return this.canUseFastVertex;
        }

        @Inject(method={"<init>"}, at={@At(value="RETURN")})
        private void checkDelegates(class_4588[] vertexConsumers, CallbackInfo ci) {
            for (class_4588 delegate : this.comp_2847) {
                if (ExtendedVertexBuilder.of(delegate) != null) continue;
                this.canUseFastVertex = false;
                return;
            }
            this.canUseFastVertex = true;
        }

        @Override
        public void vertex(float x, float y, float z, int packedColor, float u, float v, int overlay, int light, int packedNormal) {
            for (class_4588 vertexConsumer : this.comp_2847) {
                ExtendedVertexBuilder extendedVertexBuilder = (ExtendedVertexBuilder)vertexConsumer;
                extendedVertexBuilder.vertex(x, y, z, packedColor, u, v, overlay, light, packedNormal);
            }
        }
    }

    @Mixin(targets={"net/minecraft/class_4720$class_4589"})
    public static class DoubleM
    implements ExtendedVertexBuilder {
        @Shadow
        @Final
        private class_4588 field_21685;
        @Shadow
        @Final
        private class_4588 field_21686;
        @Unique
        private ExtendedVertexBuilder firstExt;
        @Unique
        private ExtendedVertexBuilder secondExt;
        @Unique
        private boolean canUseFastVertex = false;

        @Override
        public boolean canUseFastVertex() {
            return this.canUseFastVertex;
        }

        @Inject(method={"<init>"}, at={@At(value="RETURN")})
        private void checkDelegates(class_4588 vertexConsumer, class_4588 vertexConsumer2, CallbackInfo ci) {
            boolean bl = this.canUseFastVertex = ExtendedVertexBuilder.of(this.field_21685) != null && ExtendedVertexBuilder.of(this.field_21686) != null;
            if (this.canUseFastVertex) {
                this.firstExt = ExtendedVertexBuilder.of(this.field_21685);
                this.secondExt = ExtendedVertexBuilder.of(this.field_21686);
            }
        }

        @Override
        public void vertex(float x, float y, float z, int packedColor, float u, float v, int overlay, int light, int packedNormal) {
            this.firstExt.vertex(x, y, z, packedColor, u, v, overlay, light, packedNormal);
            this.secondExt.vertex(x, y, z, packedColor, u, v, overlay, light, packedNormal);
        }
    }
}

