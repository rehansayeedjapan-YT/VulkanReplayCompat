/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3i
 */
package net.vulkanmod.render.chunk.frustum;

import java.util.Arrays;
import net.vulkanmod.render.chunk.ChunkArea;
import net.vulkanmod.render.chunk.ChunkAreaManager;
import net.vulkanmod.render.chunk.frustum.VFrustum;
import org.joml.Vector3i;

public class FrustumOctree {
    static final int LEVELS = 2;

    public static void updateFrustumVisibility(VFrustum frustum, ChunkArea[] chunkAreas) {
        int width = 1 << ChunkAreaManager.AREA_SH_XZ + 4;
        for (ChunkArea chunkArea : chunkAreas) {
            Vector3i position = chunkArea.getPosition();
            int minX2 = position.x;
            int minY2 = position.y;
            int minZ2 = position.z;
            int frustumResult = frustum.cubeInFrustum(minX2, minY2, minZ2, minX2 + width, minY2 + width, minZ2 + width);
            byte[] buffer = chunkArea.getFrustumBuffer();
            if (frustumResult != -1) {
                Arrays.fill(buffer, (byte)frustumResult);
                continue;
            }
            FrustumOctree.innerCube(frustum, buffer, 2, minX2, minY2, minZ2, width, 0);
        }
    }

    static void innerCube(VFrustum frustum, byte[] buffer, int level, float xMin, float yMin, float zMin, int prevWidth, int beginIdx) {
        if (level == 1) {
            FrustumOctree.lastInnerCube(frustum, buffer, xMin, yMin, zMin, prevWidth, beginIdx);
            return;
        }
        int width = prevWidth >> 1;
        int lvlShift = (level - 1) * 3;
        for (int x = 0; x < 2; ++x) {
            float xMin2 = xMin + (float)(x * width);
            float xMax2 = xMin2 + (float)width;
            for (int y = 0; y < 2; ++y) {
                float yMin2 = yMin + (float)(y * width);
                float yMax2 = yMin2 + (float)width;
                for (int z = 0; z < 2; ++z) {
                    float zMin2 = zMin + (float)(z * width);
                    float zMax2 = zMin2 + (float)width;
                    int frustumResult = frustum.cubeInFrustum(xMin2, yMin2, zMin2, xMax2, yMax2, zMax2);
                    int idx = beginIdx + FrustumOctree.getOffset(lvlShift, x, y, z);
                    int endIdx = idx + (1 << lvlShift);
                    if (frustumResult != -1) {
                        FrustumOctree.fillResultBuffer(buffer, idx, endIdx, (byte)frustumResult);
                        continue;
                    }
                    FrustumOctree.innerCube(frustum, buffer, level - 1, xMin2, yMin2, zMin2, width, idx);
                }
            }
        }
    }

    static void lastInnerCube(VFrustum frustum, byte[] buffer, float xMin, float yMin, float zMin, int prevWidth, int beginIdx) {
        int width = prevWidth >> 1;
        for (int x = 0; x < 2; ++x) {
            float xMin2 = xMin + (float)(x * width);
            float xMax2 = xMin2 + (float)width;
            for (int y = 0; y < 2; ++y) {
                float yMin2 = yMin + (float)(y * width);
                float yMax2 = yMin2 + (float)width;
                for (int z = 0; z < 2; ++z) {
                    float zMin2 = zMin + (float)(z * width);
                    float zMax2 = zMin2 + (float)width;
                    int frustumResult = frustum.cubeInFrustum(xMin2, yMin2, zMin2, xMax2, yMax2, zMax2);
                    int idx = beginIdx + (x << 2) + (y << 1) + z;
                    buffer[idx] = (byte)frustumResult;
                }
            }
        }
    }

    static int getOffset(int baseShift, int x, int y, int z) {
        return (x << 2 + baseShift) + (y << 1 + baseShift) + (z << baseShift);
    }

    static void fillResultBuffer(byte[] buffer, int beginIdx, int endIdx, byte result) {
        for (int i = beginIdx; i < endIdx; ++i) {
            buffer[i] = result;
        }
    }
}

