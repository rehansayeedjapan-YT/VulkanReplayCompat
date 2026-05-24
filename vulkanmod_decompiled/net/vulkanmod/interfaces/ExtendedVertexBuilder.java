/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_4588
 */
package net.vulkanmod.interfaces;

import net.minecraft.class_4588;

public interface ExtendedVertexBuilder {
    public static ExtendedVertexBuilder of(class_4588 vertexConsumer) {
        if (vertexConsumer instanceof ExtendedVertexBuilder) {
            return (ExtendedVertexBuilder)vertexConsumer;
        }
        return null;
    }

    default public boolean canUseFastVertex() {
        return true;
    }

    public void vertex(float var1, float var2, float var3, int var4, float var5, float var6, int var7, int var8, int var9);

    default public void vertex(float x, float y, float z, float u, float v, int packedColor, int light) {
    }
}

