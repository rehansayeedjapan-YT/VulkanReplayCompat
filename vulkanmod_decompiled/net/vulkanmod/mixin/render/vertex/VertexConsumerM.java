/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_5611
 *  net.minecraft.class_765
 *  net.minecraft.class_777
 *  org.joml.Matrix4f
 *  org.joml.Vector3fc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.vulkanmod.mixin.render.vertex;

import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5611;
import net.minecraft.class_765;
import net.minecraft.class_777;
import net.vulkanmod.mixin.matrix.PoseAccessor;
import net.vulkanmod.render.util.MathUtil;
import net.vulkanmod.render.vertex.format.I32_SNorm;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={class_4588.class})
public interface VertexConsumerM {
    @Shadow
    public void method_23919(float var1, float var2, float var3, int var4, float var5, float var6, int var7, int var8, float var9, float var10, float var11);

    @Overwrite
    default public void method_22920(class_4587.class_4665 pose, class_777 bakedQuad, float[] brightness, float r, float g, float b, float a, int[] lights, int overlay) {
        Vector3fc vector3fc = bakedQuad.comp_3723().method_68072();
        Matrix4f matrix4f = pose.method_23761();
        boolean trustedNormals = ((PoseAccessor)pose).trustedNormals();
        int packedNormal = MathUtil.packTransformedNorm(pose.method_23762(), trustedNormals, vector3fc.x(), vector3fc.y(), vector3fc.z());
        int lightEmission = bakedQuad.comp_3726();
        for (int l = 0; l < 4; ++l) {
            Vector3fc quadPos = bakedQuad.method_76648(l);
            long packedUV = bakedQuad.method_76649(l);
            float br = brightness[l];
            int color = ColorUtil.RGBA.pack(r * br, g * br, b * br, a);
            int light = class_765.method_62228((int)lights[l], (int)lightEmission);
            float x = quadPos.x();
            float y = quadPos.y();
            float z = quadPos.z();
            float tx = MathUtil.transformX(matrix4f, x, y, z);
            float ty = MathUtil.transformY(matrix4f, x, y, z);
            float tz = MathUtil.transformZ(matrix4f, x, y, z);
            float u = class_5611.method_76641((long)packedUV);
            float v = class_5611.method_76642((long)packedUV);
            this.method_23919(tx, ty, tz, color, u, v, overlay, light, I32_SNorm.unpackX(packedNormal), I32_SNorm.unpackY(packedNormal), I32_SNorm.unpackZ(packedNormal));
        }
    }
}

