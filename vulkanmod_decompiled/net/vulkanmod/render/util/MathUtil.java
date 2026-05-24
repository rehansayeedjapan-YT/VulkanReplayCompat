/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Math
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 */
package net.vulkanmod.render.util;

import net.vulkanmod.render.vertex.format.I32_SNorm;
import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class MathUtil {
    public static float clamp(float min, float max, float x) {
        return Math.min((float)Math.max((float)x, (float)min), (float)max);
    }

    public static int clamp(int min, int max, int x) {
        return Math.min((int)Math.max((int)x, (int)min), (int)max);
    }

    public static float saturate(float x) {
        return MathUtil.clamp(0.0f, 1.0f, x);
    }

    public static float lerp(float v0, float v1, float t) {
        return v0 + t * (v1 - v0);
    }

    public static float transformX(Matrix4f mat, float x, float y, float z) {
        return Math.fma((float)mat.m00(), (float)x, (float)Math.fma((float)mat.m10(), (float)y, (float)Math.fma((float)mat.m20(), (float)z, (float)mat.m30())));
    }

    public static float transformY(Matrix4f mat, float x, float y, float z) {
        return Math.fma((float)mat.m01(), (float)x, (float)Math.fma((float)mat.m11(), (float)y, (float)Math.fma((float)mat.m21(), (float)z, (float)mat.m31())));
    }

    public static float transformZ(Matrix4f mat, float x, float y, float z) {
        return Math.fma((float)mat.m02(), (float)x, (float)Math.fma((float)mat.m12(), (float)y, (float)Math.fma((float)mat.m22(), (float)z, (float)mat.m32())));
    }

    public static int packTransformedNorm(Matrix3f mat, boolean trustedNormals, float x, float y, float z) {
        float nx = MathUtil.transformNormX(mat, x, y, z);
        float ny = MathUtil.transformNormY(mat, x, y, z);
        float nz = MathUtil.transformNormZ(mat, x, y, z);
        if (!trustedNormals) {
            float scalar = Math.invsqrt((float)Math.fma((float)nx, (float)nx, (float)Math.fma((float)ny, (float)ny, (float)(nz * nz))));
            nx *= scalar;
            ny *= scalar;
            nz *= scalar;
        }
        int packedNormal = I32_SNorm.packNormal(nx, ny, nz);
        return packedNormal;
    }

    public static float transformNormX(Matrix3f mat, float x, float y, float z) {
        return Math.fma((float)mat.m00(), (float)x, (float)Math.fma((float)mat.m10(), (float)y, (float)(mat.m20() * z)));
    }

    public static float transformNormY(Matrix3f mat, float x, float y, float z) {
        return Math.fma((float)mat.m01(), (float)x, (float)Math.fma((float)mat.m11(), (float)y, (float)(mat.m21() * z)));
    }

    public static float transformNormZ(Matrix3f mat, float x, float y, float z) {
        return Math.fma((float)mat.m02(), (float)x, (float)Math.fma((float)mat.m12(), (float)y, (float)(mat.m22() * z)));
    }
}

