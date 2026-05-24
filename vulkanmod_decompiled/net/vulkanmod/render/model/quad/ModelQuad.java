/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1058
 *  net.minecraft.class_2350
 */
package net.vulkanmod.render.model.quad;

import net.minecraft.class_1058;
import net.minecraft.class_2350;
import net.vulkanmod.render.chunk.cull.QuadFacing;
import net.vulkanmod.render.model.quad.ModelQuadView;

public class ModelQuad
implements ModelQuadView {
    public static final int VERTEX_SIZE = 8;
    private final int[] data = new int[32];
    class_2350 direction;
    class_1058 sprite;
    private int flags;

    public static int vertexOffset(int vertexIndex) {
        return vertexIndex * 8;
    }

    @Override
    public int getFlags() {
        return this.flags;
    }

    @Override
    public float getX(int idx) {
        return Float.intBitsToFloat(this.data[ModelQuad.vertexOffset(idx)]);
    }

    @Override
    public float getY(int idx) {
        return Float.intBitsToFloat(this.data[ModelQuad.vertexOffset(idx) + 1]);
    }

    @Override
    public float getZ(int idx) {
        return Float.intBitsToFloat(this.data[ModelQuad.vertexOffset(idx) + 2]);
    }

    @Override
    public int getColor(int idx) {
        return this.data[ModelQuad.vertexOffset(idx) + 3];
    }

    @Override
    public float getU(int idx) {
        return Float.intBitsToFloat(this.data[ModelQuad.vertexOffset(idx) + 4]);
    }

    @Override
    public float getV(int idx) {
        return Float.intBitsToFloat(this.data[ModelQuad.vertexOffset(idx) + 5]);
    }

    @Override
    public int getColorIndex() {
        return -1;
    }

    @Override
    public class_2350 getFacingDirection() {
        return this.direction;
    }

    @Override
    public class_2350 lightFace() {
        return this.direction;
    }

    @Override
    public QuadFacing getQuadFacing() {
        return QuadFacing.UNDEFINED;
    }

    @Override
    public int getNormal() {
        return 0;
    }

    public float setX(int idx, float f) {
        int n = Float.floatToRawIntBits(f);
        this.data[ModelQuad.vertexOffset((int)idx)] = n;
        return n;
    }

    public float setY(int idx, float f) {
        int n = Float.floatToRawIntBits(f);
        this.data[ModelQuad.vertexOffset((int)idx) + 1] = n;
        return n;
    }

    public float setZ(int idx, float f) {
        int n = Float.floatToRawIntBits(f);
        this.data[ModelQuad.vertexOffset((int)idx) + 2] = n;
        return n;
    }

    public float setU(int idx, float f) {
        int n = Float.floatToRawIntBits(f);
        this.data[ModelQuad.vertexOffset((int)idx) + 4] = n;
        return n;
    }

    public float setV(int idx, float f) {
        int n = Float.floatToRawIntBits(f);
        this.data[ModelQuad.vertexOffset((int)idx) + 5] = n;
        return n;
    }

    public void setFlags(int f) {
        this.flags = f;
    }

    public void setSprite(class_1058 sprite) {
        this.sprite = sprite;
    }
}

