/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormatElement
 *  net.minecraft.class_287
 *  net.minecraft.class_290
 *  net.minecraft.class_4587$class_4665
 *  net.minecraft.class_4588
 *  net.minecraft.class_5611
 *  net.minecraft.class_765
 *  net.minecraft.class_777
 *  org.joml.Matrix4f
 *  org.joml.Vector3fc
 *  org.lwjgl.system.MemoryUtil
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Shadow
 */
package net.vulkanmod.mixin.render.vertex;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5611;
import net.minecraft.class_765;
import net.minecraft.class_777;
import net.vulkanmod.interfaces.ExtendedVertexBuilder;
import net.vulkanmod.mixin.matrix.PoseAccessor;
import net.vulkanmod.render.util.MathUtil;
import net.vulkanmod.render.vertex.format.I32_SNorm;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={class_287.class})
public abstract class BufferBuilderM
implements class_4588,
ExtendedVertexBuilder {
    @Shadow
    private boolean field_21594;
    @Shadow
    private boolean field_21595;
    @Shadow
    private VertexFormat field_1565;
    @Shadow
    private int field_52077;
    @Shadow
    @Final
    private int field_52075;
    private long ptr;
    private final float[] brightness = new float[4];
    private final int[] lights = new int[4];

    @Shadow
    protected abstract long method_60805();

    @Shadow
    protected abstract long method_60798(VertexFormatElement var1);

    @Override
    public void vertex(float x, float y, float z, int packedColor, float u, float v, int overlay, int light, int packedNormal) {
        this.ptr = this.method_60805();
        if (this.field_1565 == class_290.field_1580) {
            MemoryUtil.memPutFloat((long)(this.ptr + 0L), (float)x);
            MemoryUtil.memPutFloat((long)(this.ptr + 4L), (float)y);
            MemoryUtil.memPutFloat((long)(this.ptr + 8L), (float)z);
            MemoryUtil.memPutInt((long)(this.ptr + 12L), (int)packedColor);
            MemoryUtil.memPutFloat((long)(this.ptr + 16L), (float)u);
            MemoryUtil.memPutFloat((long)(this.ptr + 20L), (float)v);
            MemoryUtil.memPutInt((long)(this.ptr + 24L), (int)overlay);
            MemoryUtil.memPutInt((long)(this.ptr + 28L), (int)light);
            MemoryUtil.memPutInt((long)(this.ptr + 32L), (int)packedNormal);
        } else {
            this.field_52077 = this.field_52075;
            this.position(x, y, z);
            this.fastColor(packedColor);
            this.fastUv(u, v);
            this.fastOverlay(overlay);
            this.light(light);
            this.fastNormal(packedNormal);
        }
    }

    @Override
    public void vertex(float x, float y, float z, float u, float v, int packedColor, int light) {
        this.ptr = this.method_60805();
        MemoryUtil.memPutFloat((long)(this.ptr + 0L), (float)x);
        MemoryUtil.memPutFloat((long)(this.ptr + 4L), (float)y);
        MemoryUtil.memPutFloat((long)(this.ptr + 8L), (float)z);
        MemoryUtil.memPutFloat((long)(this.ptr + 12L), (float)u);
        MemoryUtil.memPutFloat((long)(this.ptr + 16L), (float)v);
        MemoryUtil.memPutInt((long)(this.ptr + 20L), (int)packedColor);
        MemoryUtil.memPutInt((long)(this.ptr + 24L), (int)light);
    }

    public void position(float x, float y, float z) {
        MemoryUtil.memPutFloat((long)(this.ptr + 0L), (float)x);
        MemoryUtil.memPutFloat((long)(this.ptr + 4L), (float)y);
        MemoryUtil.memPutFloat((long)(this.ptr + 8L), (float)z);
    }

    public void fastColor(int packedColor) {
        long ptr = this.method_60798(VertexFormatElement.COLOR);
        if (ptr != -1L) {
            MemoryUtil.memPutInt((long)ptr, (int)packedColor);
        }
    }

    public void fastUv(float u, float v) {
        long ptr = this.method_60798(VertexFormatElement.UV0);
        if (ptr != -1L) {
            MemoryUtil.memPutFloat((long)ptr, (float)u);
            MemoryUtil.memPutFloat((long)(ptr + 4L), (float)v);
        }
    }

    public void fastOverlay(int o) {
        long ptr = this.method_60798(VertexFormatElement.UV1);
        if (ptr != -1L) {
            MemoryUtil.memPutInt((long)ptr, (int)o);
        }
    }

    public void light(int l) {
        long ptr = this.method_60798(VertexFormatElement.UV2);
        if (ptr != -1L) {
            MemoryUtil.memPutInt((long)ptr, (int)l);
        }
    }

    public void fastNormal(int packedNormal) {
        long ptr = this.method_60798(VertexFormatElement.NORMAL);
        if (ptr != -1L) {
            MemoryUtil.memPutInt((long)ptr, (int)packedNormal);
        }
    }

    @Overwrite
    public void method_23919(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
        if (this.field_21594) {
            int i;
            long ptr = this.method_60805();
            MemoryUtil.memPutFloat((long)(ptr + 0L), (float)x);
            MemoryUtil.memPutFloat((long)(ptr + 4L), (float)y);
            MemoryUtil.memPutFloat((long)(ptr + 8L), (float)z);
            MemoryUtil.memPutInt((long)(ptr + 12L), (int)color);
            MemoryUtil.memPutFloat((long)(ptr + 16L), (float)u);
            MemoryUtil.memPutFloat((long)(ptr + 20L), (float)v);
            if (this.field_21595) {
                MemoryUtil.memPutInt((long)(ptr + 24L), (int)overlay);
                i = 28;
            } else {
                i = 24;
            }
            MemoryUtil.memPutInt((long)(ptr + (long)i), (int)light);
            int temp = I32_SNorm.packNormal(normalX, normalY, normalZ);
            MemoryUtil.memPutInt((long)(ptr + (long)i + 4L), (int)temp);
        } else {
            super.method_23919(x, y, z, color, u, v, overlay, light, normalX, normalY, normalZ);
        }
    }

    public class_4588 method_1336(int r, int g, int b, int a) {
        long m = this.method_60798(VertexFormatElement.COLOR);
        if (m != -1L) {
            int color = BufferBuilderM.packRgba(r, g, b, a);
            MemoryUtil.memPutInt((long)m, (int)color);
        }
        return this;
    }

    public void method_22919(class_4587.class_4665 pose, class_777 bakedQuad, float r, float g, float b, float a, int light, int overlay) {
        this.brightness[0] = 1.0f;
        this.brightness[1] = 1.0f;
        this.brightness[2] = 1.0f;
        this.brightness[3] = 1.0f;
        this.lights[0] = light;
        this.lights[1] = light;
        this.lights[2] = light;
        this.lights[3] = light;
        this.method_22920(pose, bakedQuad, this.brightness, r, g, b, a, this.lights, overlay);
    }

    public void method_22920(class_4587.class_4665 pose, class_777 bakedQuad, float[] brightness, float r, float g, float b, float a, int[] lights, int overlay) {
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
            this.vertex(tx, ty, tz, color, u, v, overlay, light, packedNormal);
        }
    }

    private static int packRgba(int r, int g, int b, int a) {
        return (a & 0xFF) << 24 | (b & 0xFF) << 16 | (g & 0xFF) << 8 | r & 0xFF;
    }
}

