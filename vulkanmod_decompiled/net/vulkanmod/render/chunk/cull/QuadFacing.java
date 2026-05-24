/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2350
 *  net.minecraft.class_3532
 *  org.joml.Vector3f
 */
package net.vulkanmod.render.chunk.cull;

import net.minecraft.class_2350;
import net.minecraft.class_3532;
import net.vulkanmod.render.vertex.format.I32_SNorm;
import org.joml.Vector3f;

public enum QuadFacing {
    X_POS,
    Y_POS,
    Z_POS,
    X_NEG,
    Z_NEG,
    UNDEFINED,
    Y_NEG;

    public static final QuadFacing[] VALUES;
    public static final int COUNT;

    public static QuadFacing fromDirection(class_2350 direction) {
        return switch (direction) {
            default -> throw new MatchException(null, null);
            case class_2350.field_11033 -> Y_NEG;
            case class_2350.field_11036 -> Y_POS;
            case class_2350.field_11043 -> Z_NEG;
            case class_2350.field_11035 -> Z_POS;
            case class_2350.field_11039 -> X_NEG;
            case class_2350.field_11034 -> X_POS;
        };
    }

    public static QuadFacing fromNormal(int packedNormal) {
        float x = I32_SNorm.unpackX(packedNormal);
        float y = I32_SNorm.unpackY(packedNormal);
        float z = I32_SNorm.unpackZ(packedNormal);
        return QuadFacing.fromNormal(x, y, z);
    }

    public static QuadFacing fromNormal(Vector3f normal) {
        return QuadFacing.fromNormal(normal.x(), normal.y(), normal.z());
    }

    public static QuadFacing fromNormal(float x, float y, float z) {
        float absZ;
        float absY;
        float absX = Math.abs(x);
        float sum = absX + (absY = Math.abs(y)) + (absZ = Math.abs(z));
        if (class_3532.method_15347((float)sum, (float)1.0f)) {
            if (class_3532.method_15347((float)absX, (float)1.0f)) {
                return x > 0.0f ? X_POS : X_NEG;
            }
            if (class_3532.method_15347((float)absY, (float)1.0f)) {
                return y > 0.0f ? Y_POS : Y_NEG;
            }
            if (class_3532.method_15347((float)absZ, (float)1.0f)) {
                return z > 0.0f ? Z_POS : Z_NEG;
            }
        }
        return UNDEFINED;
    }

    static {
        VALUES = QuadFacing.values();
        COUNT = VALUES.length;
    }
}

