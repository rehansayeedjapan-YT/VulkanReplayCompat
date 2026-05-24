/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_3532
 *  org.joml.Vector3i
 */
package net.vulkanmod.render.chunk;

import java.util.EnumMap;
import net.minecraft.class_3532;
import net.vulkanmod.render.chunk.ChunkArea;
import net.vulkanmod.render.chunk.RenderSection;
import net.vulkanmod.render.chunk.buffer.AreaBuffer;
import net.vulkanmod.render.chunk.buffer.DrawBuffers;
import net.vulkanmod.render.chunk.frustum.FrustumOctree;
import net.vulkanmod.render.chunk.frustum.VFrustum;
import net.vulkanmod.render.chunk.util.CircularIntList;
import net.vulkanmod.render.chunk.util.Util;
import net.vulkanmod.render.vertex.TerrainRenderType;
import org.joml.Vector3i;

public class ChunkAreaManager {
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;
    public static final int AREA_SIZE = 512;
    public static final int AREA_SH_XZ = Util.flooredLog(8);
    public static final int AREA_SH_Y = Util.flooredLog(8);
    public static final int SEC_SH = 4;
    public static final int BLOCK_TO_AREA_SH_XZ = AREA_SH_XZ + 4;
    public static final int BLOCK_TO_AREA_SH_Y = AREA_SH_Y + 4;
    public final int size;
    final int sectionGridWidth;
    final int xzSize;
    final int ySize;
    final int minHeight;
    final ChunkArea[] chunkAreasArr;
    int prevX;
    int prevZ;
    private final CircularIntList xList;
    private final CircularIntList zList;
    private final CircularIntList.RangeIterator xComplIterator;

    public ChunkAreaManager(int width, int height, int minHeight) {
        this.minHeight = minHeight;
        this.sectionGridWidth = width;
        int t = (width >> AREA_SH_XZ) + 2;
        int relativeHeight = height - (minHeight >> 4);
        int n = this.ySize = (relativeHeight & 5) == 0 ? relativeHeight >> AREA_SH_Y : (relativeHeight >> AREA_SH_Y) + 1;
        if ((t & 1) == 0) {
            ++t;
        }
        this.xzSize = t;
        this.size = this.xzSize * this.ySize * this.xzSize;
        this.chunkAreasArr = new ChunkArea[this.size];
        for (int z = 0; z < this.xzSize; ++z) {
            for (int y = 0; y < this.ySize; ++y) {
                for (int x = 0; x < this.xzSize; ++x) {
                    int idx = this.getAreaIndex(x, y, z);
                    Vector3i origin = new Vector3i(x << BLOCK_TO_AREA_SH_XZ, y << BLOCK_TO_AREA_SH_Y, z << BLOCK_TO_AREA_SH_XZ);
                    this.chunkAreasArr[idx] = new ChunkArea(idx, origin, minHeight);
                }
            }
        }
        this.prevX = Integer.MIN_VALUE;
        this.prevZ = Integer.MIN_VALUE;
        this.xList = new CircularIntList(this.xzSize);
        this.zList = new CircularIntList(this.xzSize);
        this.xComplIterator = this.xList.createRangeIterator();
    }

    public void repositionAreas(int secX, int secZ) {
        int yRel;
        int z1;
        int zRelativeIndex;
        int x1;
        int xRelativeIndex;
        int zRangeEnd;
        int zRangeStart;
        int xComplEnd;
        int xComplStart;
        int xRangeEnd;
        int xRangeStart;
        int xS = secX >> AREA_SH_XZ;
        int zS = secZ >> AREA_SH_XZ;
        int deltaX = class_3532.method_15340((int)(xS - this.prevX), (int)(-this.xzSize), (int)this.xzSize);
        int deltaZ = class_3532.method_15340((int)(zS - this.prevZ), (int)(-this.xzSize), (int)this.xzSize);
        int xAbsChunkIndex = xS - this.xzSize / 2;
        int xStart = Math.floorMod(xAbsChunkIndex, this.xzSize);
        int zAbsChunkIndex = zS - this.xzSize / 2;
        int zStart = Math.floorMod(zAbsChunkIndex, this.xzSize);
        CircularIntList xList = this.xList;
        CircularIntList zList = this.zList;
        xList.updateStartIdx(xStart);
        zList.updateStartIdx(zStart);
        CircularIntList.OwnIterator xIterator = xList.iterator();
        CircularIntList.OwnIterator zIterator = zList.iterator();
        if (deltaX >= 0) {
            xRangeStart = this.xzSize - deltaX;
            xRangeEnd = this.xzSize - 1;
            xComplStart = 0;
            xComplEnd = xRangeStart - 1;
        } else {
            xRangeStart = 0;
            xComplStart = xRangeEnd = -deltaX - 1;
            xComplEnd = this.xzSize - 1;
        }
        if (deltaZ >= 0) {
            zRangeStart = this.xzSize - deltaZ;
            zRangeEnd = this.xzSize - 1;
        } else {
            zRangeStart = 0;
            zRangeEnd = -deltaZ - 1;
        }
        CircularIntList.RangeIterator xRangeIterator = xList.getRangeIterator(xRangeStart, xRangeEnd);
        CircularIntList.RangeIterator zRangeIterator = zList.getRangeIterator(zRangeStart, zRangeEnd);
        CircularIntList.RangeIterator xComplIterator = this.xComplIterator;
        xComplIterator.update(xComplStart, xComplEnd);
        xAbsChunkIndex = xS - this.xzSize / 2 + xRangeStart;
        while (xRangeIterator.hasNext()) {
            xRelativeIndex = xRangeIterator.next();
            x1 = xAbsChunkIndex << AREA_SH_XZ + 4;
            zIterator.restart();
            zAbsChunkIndex = zS - (this.xzSize >> 1);
            while (zIterator.hasNext()) {
                zRelativeIndex = zIterator.next();
                z1 = zAbsChunkIndex << AREA_SH_XZ + 4;
                for (yRel = 0; yRel < this.ySize; ++yRel) {
                    this.moveArea(xRelativeIndex, yRel, zRelativeIndex, x1, z1);
                }
                ++zAbsChunkIndex;
            }
            ++xAbsChunkIndex;
        }
        xAbsChunkIndex = xS - this.xzSize / 2 + xComplStart;
        while (xComplIterator.hasNext()) {
            xRelativeIndex = xComplIterator.next();
            x1 = xAbsChunkIndex << AREA_SH_XZ + 4;
            zRangeIterator.restart();
            zAbsChunkIndex = zS - (this.xzSize >> 1) + zRangeStart;
            while (zRangeIterator.hasNext()) {
                zRelativeIndex = zRangeIterator.next();
                z1 = zAbsChunkIndex << AREA_SH_XZ + 4;
                for (yRel = 0; yRel < this.ySize; ++yRel) {
                    this.moveArea(xRelativeIndex, yRel, zRelativeIndex, x1, z1);
                }
                ++zAbsChunkIndex;
            }
            ++xAbsChunkIndex;
        }
        this.prevX = xS;
        this.prevZ = zS;
    }

    private void moveArea(int xRelativeIndex, int yRel, int zRelativeIndex, int x1, int z1) {
        int y1 = this.minHeight + (yRel << AREA_SH_Y + 4);
        ChunkArea chunkArea = this.chunkAreasArr[this.getAreaIndex(xRelativeIndex, yRel, zRelativeIndex)];
        chunkArea.setPosition(x1, y1, z1);
        chunkArea.releaseBuffers();
    }

    public ChunkArea getChunkArea(RenderSection section, int x, int y, int z) {
        int shX = AREA_SH_XZ + 4;
        int shY = AREA_SH_Y + 4;
        int shZ = AREA_SH_XZ + 4;
        int AreaX = x >> shX;
        int AreaY = y - this.minHeight >> shY;
        int AreaZ = z >> shZ;
        int x1 = Math.floorMod(AreaX, this.xzSize);
        int z1 = Math.floorMod(AreaZ, this.xzSize);
        ChunkArea chunkArea = this.chunkAreasArr[this.getAreaIndex(x1, AreaY, z1)];
        return chunkArea;
    }

    public ChunkArea getChunkArea(int idx) {
        return idx >= 0 && idx < this.chunkAreasArr.length ? this.chunkAreasArr[idx] : null;
    }

    public void updateFrustumVisibility(VFrustum frustum) {
        FrustumOctree.updateFrustumVisibility(frustum, this.chunkAreasArr);
    }

    public void resetQueues() {
        for (ChunkArea chunkArea : this.chunkAreasArr) {
            chunkArea.resetQueue();
        }
    }

    private int getAreaIndex(int x, int y, int z) {
        return (z * this.ySize + y) * this.xzSize + x;
    }

    public void freeAllBuffers() {
        for (ChunkArea chunkArea : this.chunkAreasArr) {
            chunkArea.free();
        }
    }

    public String[] getStats() {
        long vbSize = 0L;
        long ibSize = 0L;
        long frag = 0L;
        long vbUsed = 0L;
        long ibUsed = 0L;
        int count = 0;
        for (ChunkArea chunkArea : this.chunkAreasArr) {
            DrawBuffers drawBuffers = chunkArea.drawBuffers;
            if (!drawBuffers.isAllocated()) continue;
            EnumMap<TerrainRenderType, AreaBuffer> vertexBuffers = drawBuffers.getVertexBuffers();
            for (AreaBuffer buffer : vertexBuffers.values()) {
                vbSize += (long)buffer.getSize();
                vbUsed += (long)buffer.getUsed();
                frag += (long)buffer.fragmentation();
            }
            AreaBuffer indexBuffer = drawBuffers.getIndexBuffer();
            if (indexBuffer != null) {
                ibSize += (long)indexBuffer.getSize();
                ibUsed += (long)indexBuffer.getUsed();
                frag += (long)indexBuffer.fragmentation();
            }
            ++count;
        }
        int div = 0x100000;
        return new String[]{String.format("Vertex Buffers: %d/%d MB", vbUsed /= 0x100000L, vbSize /= 0x100000L), String.format("Index Buffers: %d/%d MB", ibUsed /= 0x100000L, ibSize /= 0x100000L), String.format("Allocations: %d Frag: %d MB", count, frag /= 0x100000L)};
    }
}

