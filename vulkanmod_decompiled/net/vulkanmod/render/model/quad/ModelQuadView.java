/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2350
 */
package net.vulkanmod.render.model.quad;

import net.minecraft.class_2350;
import net.vulkanmod.render.chunk.cull.QuadFacing;

public interface ModelQuadView {
    public int getFlags();

    public float getX(int var1);

    public float getY(int var1);

    public float getZ(int var1);

    public int getColor(int var1);

    public float getU(int var1);

    public float getV(int var1);

    public int getColorIndex();

    public class_2350 getFacingDirection();

    public class_2350 lightFace();

    public QuadFacing getQuadFacing();

    public int getNormal();

    default public boolean isTinted() {
        return this.getColorIndex() != -1;
    }
}

