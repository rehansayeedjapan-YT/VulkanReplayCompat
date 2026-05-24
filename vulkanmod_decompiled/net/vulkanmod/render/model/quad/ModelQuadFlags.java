/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2350
 *  net.minecraft.class_2350$class_2351
 */
package net.vulkanmod.render.model.quad;

import net.minecraft.class_2350;
import net.vulkanmod.render.model.quad.ModelQuadView;

public class ModelQuadFlags {
    public static final int IS_PARTIAL = 1;
    public static final int IS_PARALLEL = 2;
    public static final int IS_ALIGNED = 4;

    public static boolean contains(int flags, int mask) {
        return (flags & mask) != 0;
    }

    public static int getQuadFlags(ModelQuadView quad, class_2350 face) {
        boolean bl;
        boolean parallel;
        boolean partial;
        block32: {
            block31: {
                float minX = 32.0f;
                float minY = 32.0f;
                float minZ = 32.0f;
                float maxX = -32.0f;
                float maxY = -32.0f;
                float maxZ = -32.0f;
                for (int i = 0; i < 4; ++i) {
                    float x = quad.getX(i);
                    float y = quad.getY(i);
                    float z = quad.getZ(i);
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    minZ = Math.min(minZ, z);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                    maxZ = Math.max(maxZ, z);
                }
                partial = switch (face.method_10166()) {
                    default -> throw new MatchException(null, null);
                    case class_2350.class_2351.field_11048 -> {
                        if (minY >= 1.0E-4f || minZ >= 1.0E-4f || maxY <= 0.9999f || maxZ <= 0.9999f) {
                            yield true;
                        }
                        yield false;
                    }
                    case class_2350.class_2351.field_11052 -> {
                        if (minX >= 1.0E-4f || minZ >= 1.0E-4f || maxX <= 0.9999f || maxZ <= 0.9999f) {
                            yield true;
                        }
                        yield false;
                    }
                    case class_2350.class_2351.field_11051 -> minX >= 1.0E-4f || minY >= 1.0E-4f || maxX <= 0.9999f || maxY <= 0.9999f;
                };
                switch (face.method_10166()) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case field_11048: {
                        boolean bl2;
                        if (minX == maxX) {
                            bl2 = true;
                            break;
                        }
                        bl2 = false;
                        break;
                    }
                    case field_11052: {
                        boolean bl2;
                        if (minY == maxY) {
                            bl2 = true;
                            break;
                        }
                        bl2 = false;
                        break;
                    }
                    case field_11051: {
                        boolean bl2 = parallel = minZ == maxZ;
                    }
                }
                if (!parallel) break block31;
                switch (face) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case field_11033: {
                        if (minY < 1.0E-4f) {
                            break;
                        }
                        break block31;
                    }
                    case field_11036: {
                        if (maxY > 0.9999f) {
                            break;
                        }
                        break block31;
                    }
                    case field_11043: {
                        if (minZ < 1.0E-4f) {
                            break;
                        }
                        break block31;
                    }
                    case field_11035: {
                        if (maxZ > 0.9999f) {
                            break;
                        }
                        break block31;
                    }
                    case field_11039: {
                        if (minX < 1.0E-4f) {
                            break;
                        }
                        break block31;
                    }
                    case field_11034: {
                        if (!(maxX > 0.9999f)) break block31;
                    }
                }
                bl = true;
                break block32;
            }
            bl = false;
        }
        boolean aligned = bl;
        int flags = 0;
        if (partial) {
            flags |= 1;
        }
        if (parallel) {
            flags |= 2;
        }
        if (aligned) {
            flags |= 4;
        }
        return flags;
    }
}

