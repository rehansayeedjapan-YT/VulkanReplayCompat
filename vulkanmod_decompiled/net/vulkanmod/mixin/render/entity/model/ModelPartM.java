/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_630
 *  net.minecraft.class_630$class_628
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.vulkanmod.mixin.render.entity.model;

import java.util.List;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_630;
import net.vulkanmod.interfaces.ExtendedVertexBuilder;
import net.vulkanmod.interfaces.ModelPartCubeMixed;
import net.vulkanmod.render.model.CubeModel;
import net.vulkanmod.render.vertex.format.I32_SNorm;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_630.class})
public abstract class ModelPartM {
    @Shadow
    @Final
    private List<class_630.class_628> field_3663;
    @Unique
    Vector3f normal = new Vector3f();

    @Inject(method={"method_22702"}, at={@At(value="HEAD")}, cancellable=true)
    private void injCompile(class_4587.class_4665 pose, class_4588 vertexConsumer, int light, int overlay, int color, CallbackInfo ci) {
        this.renderCubes(pose, vertexConsumer, light, overlay, color);
        ci.cancel();
    }

    @Unique
    public void renderCubes(class_4587.class_4665 pose, class_4588 vertexConsumer, int light, int overlay, int color) {
        boolean useFastFormat;
        Matrix4f matrix4f = pose.method_23761();
        Matrix3f matrix3f = pose.method_23762();
        ExtendedVertexBuilder vertexBuilder = ExtendedVertexBuilder.of(vertexConsumer);
        boolean bl = useFastFormat = vertexBuilder != null && vertexBuilder.canUseFastVertex();
        if (useFastFormat) {
            color = ColorUtil.RGBA.fromArgb32(color);
            for (class_630.class_628 cube : this.field_3663) {
                ModelPartCubeMixed cubeMixed = (ModelPartCubeMixed)cube;
                CubeModel cubeModel = cubeMixed.getCubeModel();
                CubeModel.Polygon[] polygons = cubeModel.getPolygons();
                cubeModel.transformVertices(matrix4f);
                for (CubeModel.Polygon polygon : polygons) {
                    CubeModel.Vertex[] vertices;
                    matrix3f.transform(this.normal.set(polygon.normal()));
                    this.normal.normalize();
                    int packedNormal = I32_SNorm.packNormal(this.normal.x(), this.normal.y(), this.normal.z());
                    for (CubeModel.Vertex vertex : vertices = polygon.vertices()) {
                        Vector3f pos = vertex.pos();
                        vertexBuilder.vertex(pos.x(), pos.y(), pos.z(), color, vertex.u(), vertex.v(), overlay, light, packedNormal);
                    }
                }
            }
        } else {
            for (class_630.class_628 cube : this.field_3663) {
                ModelPartCubeMixed cubeMixed = (ModelPartCubeMixed)cube;
                CubeModel cubeModel = cubeMixed.getCubeModel();
                CubeModel.Polygon[] polygons = cubeModel.getPolygons();
                cubeModel.transformVertices(matrix4f);
                for (CubeModel.Polygon polygon : polygons) {
                    CubeModel.Vertex[] vertices;
                    matrix3f.transform(this.normal.set(polygon.normal()));
                    this.normal.normalize();
                    for (CubeModel.Vertex vertex : vertices = polygon.vertices()) {
                        Vector3f pos = vertex.pos();
                        vertexConsumer.method_23919(pos.x(), pos.y(), pos.z(), color, vertex.u(), vertex.v(), overlay, light, this.normal.x(), this.normal.y(), this.normal.z());
                    }
                }
            }
        }
    }
}

