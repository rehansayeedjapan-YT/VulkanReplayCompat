/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.render.vertex.format;

public abstract class I32_SNorm {
    private static final float NORM_INV = 0.007874016f;

    public static int packNormal(float x, float y, float z) {
        return (int)(x *= 127.0f) & 0xFF | ((int)(y *= 127.0f) & 0xFF) << 8 | ((int)(z *= 127.0f) & 0xFF) << 16;
    }

    public static int packNormal(int x, int y, int z) {
        return x & 0xFF | (y & 0xFF) << 8 | (z & 0xFF) << 16;
    }

    public static float unpackX(int i) {
        return (float)((byte)(i & 0xFF)) * 0.007874016f;
    }

    public static float unpackY(int i) {
        return (float)((byte)(i >> 8 & 0xFF)) * 0.007874016f;
    }

    public static float unpackZ(int i) {
        return (float)((byte)(i >> 16 & 0xFF)) * 0.007874016f;
    }
}

