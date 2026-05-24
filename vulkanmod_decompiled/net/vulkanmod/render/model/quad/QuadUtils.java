/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.render.model.quad;

public abstract class QuadUtils {
    public static final byte DEFAULT_START_IDX = 0;
    public static final byte FLIPPED_START_IDX = 3;

    public static int getIterationStartIdx(float[] aos, int[] lms) {
        float ao00_11 = aos[0] + aos[2];
        float ao10_01 = aos[1] + aos[3];
        if (ao00_11 > ao10_01) {
            return 0;
        }
        if (ao00_11 < ao10_01) {
            return 3;
        }
        float lm00_11 = lms[0] + lms[2];
        float lm10_01 = lms[1] + lms[3];
        if (lm00_11 >= lm10_01) {
            return 3;
        }
        return 0;
    }

    public static int getIterationStartIdx(float[] aos) {
        float ao00_11 = aos[0] + aos[2];
        float ao10_01 = aos[1] + aos[3];
        if (ao00_11 >= ao10_01) {
            return 0;
        }
        return 3;
    }
}

