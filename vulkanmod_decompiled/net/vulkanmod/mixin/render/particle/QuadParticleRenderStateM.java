/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11944
 *  net.minecraft.class_4588
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Overwrite
 *  org.spongepowered.asm.mixin.Unique
 */
package net.vulkanmod.mixin.render.particle;

import net.minecraft.class_11944;
import net.minecraft.class_4588;
import net.vulkanmod.interfaces.ExtendedVertexBuilder;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={class_11944.class})
public class QuadParticleRenderStateM {
    @Unique
    private final Quaternionf quaternionf = new Quaternionf();
    @Unique
    private final Vector3f vector3f = new Vector3f();

    @Overwrite
    public void method_74321(class_4588 vertexConsumer, float x, float y, float z, float xr, float yr, float zr, float wr, float m, float u0, float u1, float v0, float v1, int color, int light) {
        this.quaternionf.set(xr, yr, zr, wr);
        ExtendedVertexBuilder vertexBuilder = (ExtendedVertexBuilder)vertexConsumer;
        color = ColorUtil.BGRAtoRGBA(color);
        this.renderVertex(vertexBuilder, this.quaternionf, x, y, z, 1.0f, -1.0f, m, u1, v1, color, light);
        this.renderVertex(vertexBuilder, this.quaternionf, x, y, z, 1.0f, 1.0f, m, u1, v0, color, light);
        this.renderVertex(vertexBuilder, this.quaternionf, x, y, z, -1.0f, 1.0f, m, u0, v0, color, light);
        this.renderVertex(vertexBuilder, this.quaternionf, x, y, z, -1.0f, -1.0f, m, u0, v1, color, light);
    }

    private void renderVertex(ExtendedVertexBuilder vertexConsumer, Quaternionf quaternionf, float x, float y, float z, float i, float j, float k, float u, float v, int color, int light) {
        this.vector3f.set(i, j, 0.0f).rotate((Quaternionfc)quaternionf).mul(k).add(x, y, z);
        vertexConsumer.vertex(this.vector3f.x(), this.vector3f.y(), this.vector3f.z(), u, v, color, light);
    }
}

