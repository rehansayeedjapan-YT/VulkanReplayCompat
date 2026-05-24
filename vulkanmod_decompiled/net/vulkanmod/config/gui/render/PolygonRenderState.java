/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  net.minecraft.class_11231
 *  net.minecraft.class_11244
 *  net.minecraft.class_4588
 *  net.minecraft.class_8030
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fc
 */
package net.vulkanmod.config.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_4588;
import net.minecraft.class_8030;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public final class PolygonRenderState
extends Record
implements class_11244 {
    private final RenderPipeline pipeline;
    private final class_11231 textureSetup;
    private final Matrix3x2f pose;
    private final float[][] vertices;
    private final int col;
    @Nullable
    private final class_8030 scissorArea;
    @Nullable
    private final class_8030 bounds;

    public PolygonRenderState(RenderPipeline renderPipeline, class_11231 textureSetup, Matrix3x2f pose, float[][] vertices, int color, @Nullable class_8030 screenRectangle) {
        this(renderPipeline, textureSetup, pose, vertices, color, screenRectangle, PolygonRenderState.getBounds(vertices, pose, screenRectangle));
    }

    public PolygonRenderState(RenderPipeline pipeline, class_11231 textureSetup, Matrix3x2f pose, float[][] vertices, int col, @Nullable class_8030 scissorArea, @Nullable class_8030 bounds) {
        this.pipeline = pipeline;
        this.textureSetup = textureSetup;
        this.pose = pose;
        this.vertices = vertices;
        this.col = col;
        this.scissorArea = scissorArea;
        this.bounds = bounds;
    }

    public void method_70917(class_4588 vertexConsumer) {
        for (float[] vertex : this.vertices) {
            float x = vertex[0];
            float y = vertex[1];
            vertexConsumer.method_70815((Matrix3x2fc)this.pose(), x, y).method_39415(this.col);
        }
    }

    @Nullable
    private static class_8030 getBounds(float[][] vertices, Matrix3x2f matrix3x2f, @Nullable class_8030 screenRectangle) {
        float x0 = vertices[0][0];
        float x1 = vertices[0][0];
        float y0 = vertices[0][1];
        float y1 = vertices[0][1];
        for (float[] vertex : vertices) {
            float x = vertex[0];
            float y = vertex[1];
            if (x < x0) {
                x0 = x;
            }
            if (x > x1) {
                x1 = x;
            }
            if (y < y0) {
                y0 = y;
            }
            if (!(y > y1)) continue;
            y1 = y;
        }
        class_8030 screenRectangle2 = new class_8030((int)x0, (int)y0, (int)(x1 - x0), (int)(y1 - y0)).method_71523((Matrix3x2fc)matrix3x2f);
        return screenRectangle != null ? screenRectangle.method_49701(screenRectangle2) : screenRectangle2;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{PolygonRenderState.class, "pipeline;textureSetup;pose;vertices;col;scissorArea;bounds", "pipeline", "textureSetup", "pose", "vertices", "col", "scissorArea", "bounds"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PolygonRenderState.class, "pipeline;textureSetup;pose;vertices;col;scissorArea;bounds", "pipeline", "textureSetup", "pose", "vertices", "col", "scissorArea", "bounds"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PolygonRenderState.class, "pipeline;textureSetup;pose;vertices;col;scissorArea;bounds", "pipeline", "textureSetup", "pose", "vertices", "col", "scissorArea", "bounds"}, this, o);
    }

    public RenderPipeline comp_4055() {
        return this.pipeline;
    }

    public class_11231 comp_4056() {
        return this.textureSetup;
    }

    public Matrix3x2f pose() {
        return this.pose;
    }

    public float[][] vertices() {
        return this.vertices;
    }

    public int col() {
        return this.col;
    }

    @Nullable
    public class_8030 comp_4069() {
        return this.scissorArea;
    }

    @Nullable
    public class_8030 comp_4274() {
        return this.bounds;
    }
}

