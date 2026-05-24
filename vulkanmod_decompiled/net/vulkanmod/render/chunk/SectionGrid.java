/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  org.jetbrains.annotations.Nullable
 */
package net.vulkanmod.render.chunk;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.vulkanmod.render.chunk.ChunkArea;
import net.vulkanmod.render.chunk.ChunkAreaManager;
import net.vulkanmod.render.chunk.RenderSection;
import net.vulkanmod.render.chunk.frustum.VFrustum;
import net.vulkanmod.render.chunk.util.CircularIntList;
import org.jetbrains.annotations.Nullable;

public class SectionGrid {
    protected final class_1937 level;
    protected int gridHeight;
    protected int gridWidth;
    public RenderSection[] sections;
    final ChunkAreaManager chunkAreaManager;
    private int prevSecX;
    private int prevSecZ;
    private final CircularIntList xList;
    private final CircularIntList zList;
    private final CircularIntList.RangeIterator xComplIterator;

    public SectionGrid(class_1937 level, int viewDistance) {
        this.level = level;
        this.setViewDistance(viewDistance);
        this.createChunks();
        this.chunkAreaManager = new ChunkAreaManager(this.gridWidth, this.gridHeight, this.level.method_31607());
        this.prevSecX = Integer.MIN_VALUE;
        this.prevSecZ = Integer.MIN_VALUE;
        this.xList = new CircularIntList(this.gridWidth);
        this.zList = new CircularIntList(this.gridWidth);
        this.xComplIterator = this.xList.createRangeIterator();
    }

    protected void createChunks() {
        if (!class_310.method_1551().method_18854()) {
            throw new IllegalStateException("createChunks called from wrong thread: " + Thread.currentThread().getName());
        }
        int size = this.gridWidth * this.gridHeight * this.gridWidth;
        this.sections = new RenderSection[size];
        for (int j = 0; j < this.gridWidth; ++j) {
            for (int k = 0; k < this.gridHeight; ++k) {
                for (int l = 0; l < this.gridWidth; ++l) {
                    RenderSection renderSection;
                    int i1 = this.getChunkIndex(j, k, l);
                    this.sections[i1] = renderSection = new RenderSection(i1, j * 16, k * 16, l * 16);
                }
            }
        }
        this.setYNeighbours();
    }

    public void freeAllBuffers() {
        this.chunkAreaManager.freeAllBuffers();
    }

    private int getChunkIndex(int x, int y, int z) {
        return (z * this.gridHeight + y) * this.gridWidth + x;
    }

    protected void setViewDistance(int radius) {
        int i;
        this.gridWidth = i = radius * 2 + 1;
        this.gridHeight = this.level.method_32890();
        this.gridWidth = i;
    }

    public void repositionCamera(double x, double z) {
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
        int secX = class_3532.method_15357((double)x) >> 4;
        int secZ = class_3532.method_15357((double)z) >> 4;
        this.chunkAreaManager.repositionAreas(secX, secZ);
        int dx = class_3532.method_15340((int)(secX - this.prevSecX), (int)(-this.gridWidth), (int)this.gridWidth);
        int dz = class_3532.method_15340((int)(secZ - this.prevSecZ), (int)(-this.gridWidth), (int)this.gridWidth);
        int xAbsChunkIndex = secX - this.gridWidth / 2;
        int xStart = Math.floorMod(xAbsChunkIndex, this.gridWidth);
        int zAbsChunkIndex = secZ - this.gridWidth / 2;
        int zStart = Math.floorMod(zAbsChunkIndex, this.gridWidth);
        CircularIntList xList = this.xList;
        CircularIntList zList = this.zList;
        xList.updateStartIdx(xStart);
        zList.updateStartIdx(zStart);
        CircularIntList.OwnIterator xIterator = xList.iterator();
        CircularIntList.OwnIterator zIterator = zList.iterator();
        if (dx >= 0) {
            xRangeStart = this.gridWidth - dx;
            xRangeEnd = this.gridWidth - 1;
            xComplStart = 0;
            xComplEnd = xRangeStart - 1;
        } else {
            xRangeStart = 0;
            xComplStart = xRangeEnd = -dx - 1;
            xComplEnd = this.gridWidth - 1;
        }
        if (dz >= 0) {
            zRangeStart = this.gridWidth - dz;
            zRangeEnd = this.gridWidth - 1;
        } else {
            zRangeStart = 0;
            zRangeEnd = -dz - 1;
        }
        CircularIntList.RangeIterator xRangeIterator = xList.getRangeIterator(xRangeStart, xRangeEnd);
        CircularIntList.RangeIterator zRangeIterator = zList.getRangeIterator(zRangeStart, zRangeEnd);
        CircularIntList.RangeIterator xComplIterator = this.xComplIterator;
        xComplIterator.update(xComplStart, xComplEnd);
        xAbsChunkIndex = secX - (this.gridWidth >> 1) + xRangeStart;
        while (xRangeIterator.hasNext()) {
            xRelativeIndex = xRangeIterator.next();
            x1 = xAbsChunkIndex << 4;
            zIterator.restart();
            zAbsChunkIndex = secZ - (this.gridWidth >> 1);
            while (zIterator.hasNext()) {
                zRelativeIndex = zIterator.next();
                z1 = zAbsChunkIndex << 4;
                for (yRel = 0; yRel < this.gridHeight; ++yRel) {
                    this.moveSection(xRelativeIndex, yRel, zRelativeIndex, x1, z1, xList, zList, xRangeIterator.getCurrentIndex(), zIterator.getCurrentIndex());
                }
                ++zAbsChunkIndex;
            }
            ++xAbsChunkIndex;
        }
        xAbsChunkIndex = secX - (this.gridWidth >> 1) + xComplStart;
        while (xComplIterator.hasNext()) {
            xRelativeIndex = xComplIterator.next();
            x1 = xAbsChunkIndex << 4;
            zRangeIterator.restart();
            zAbsChunkIndex = secZ - (this.gridWidth >> 1) + zRangeStart;
            while (zRangeIterator.hasNext()) {
                zRelativeIndex = zRangeIterator.next();
                z1 = zAbsChunkIndex << 4;
                for (yRel = 0; yRel < this.gridHeight; ++yRel) {
                    this.moveSection(xRelativeIndex, yRel, zRelativeIndex, x1, z1, xList, zList, xComplIterator.getCurrentIndex(), zRangeIterator.getCurrentIndex());
                }
                ++zAbsChunkIndex;
            }
            ++xAbsChunkIndex;
        }
        this.prevSecX = secX;
        this.prevSecZ = secZ;
    }

    private void moveSection(int xRelativeIndex, int yRel, int zRelativeIndex, int x1, int z1, CircularIntList xList, CircularIntList zList, int xCurrentIdx, int zCurrentIdx) {
        int y1 = this.level.method_31607() + (yRel << 4);
        RenderSection renderSection = this.sections[this.getChunkIndex(xRelativeIndex, yRel, zRelativeIndex)];
        this.unsetNeighbours(renderSection);
        renderSection.setOrigin(x1, y1, z1);
        this.setNeighbours(renderSection, xList, zList, xCurrentIdx, zCurrentIdx, xRelativeIndex, yRel, zRelativeIndex);
        ChunkArea oldArea = renderSection.getChunkArea();
        if (oldArea != null) {
            oldArea.removeSection();
        }
        ChunkArea chunkArea = this.chunkAreaManager.getChunkArea(renderSection, x1, y1, z1);
        chunkArea.addSection();
        renderSection.setChunkArea(chunkArea);
        renderSection.inAreaIndex = (short)((x1 - chunkArea.position.x() >> 4) + ((z1 - chunkArea.position.z() >> 4) * 8 + (y1 - chunkArea.position.y() >> 4)) * 8);
    }

    private void setNeighbours(RenderSection section, CircularIntList xList, CircularIntList zList, int xIdx, int zIdx, int x, int y, int z) {
        RenderSection neighbour;
        int eastX = xList.getNext(xIdx);
        int westX = xList.getPrevious(xIdx);
        int northZ = zList.getPrevious(zIdx);
        int southZ = zList.getNext(zIdx);
        if (eastX != -1) {
            neighbour = this.sections[this.getChunkIndex(eastX, y, z)];
            section.setAdjacent(neighbour, 5);
        }
        if (westX != -1) {
            neighbour = this.sections[this.getChunkIndex(westX, y, z)];
            section.setAdjacent(neighbour, 4);
        }
        if (northZ != -1) {
            neighbour = this.sections[this.getChunkIndex(x, y, northZ)];
            section.setAdjacent(neighbour, 2);
        }
        if (southZ != -1) {
            neighbour = this.sections[this.getChunkIndex(x, y, southZ)];
            section.setAdjacent(neighbour, 3);
        }
    }

    private void unsetNeighbours(RenderSection section) {
        section.adjDirs = (byte)(section.adjDirs & 3);
        for (int i = 2; i < 6; ++i) {
            section.resetAdjacent(i);
        }
    }

    private void setYNeighbours() {
        for (int j = 0; j < this.gridWidth; ++j) {
            for (int k = 0; k < this.gridHeight; ++k) {
                for (int l = 0; l < this.gridWidth; ++l) {
                    int i1 = this.getChunkIndex(j, k, l);
                    this.setYNeighbours(this.sections[i1], j, k, l);
                }
            }
        }
    }

    private void setYNeighbours(RenderSection section, int x, int y, int z) {
        RenderSection neighbour;
        if (y != this.gridHeight - 1) {
            neighbour = this.sections[this.getChunkIndex(x, y + 1, z)];
            section.setAdjacent(neighbour, 1);
        }
        if (y != 0) {
            neighbour = this.sections[this.getChunkIndex(x, y - 1, z)];
            section.setAdjacent(neighbour, 0);
        }
    }

    private void setChunkArea(RenderSection section, int x, int y, int z) {
        ChunkArea oldArea = section.getChunkArea();
        if (oldArea != null) {
            oldArea.removeSection();
        }
        ChunkArea chunkArea = this.chunkAreaManager.getChunkArea(section, x, y, z);
        chunkArea.addSection();
        section.setChunkArea(chunkArea);
    }

    public void setDirty(int sectionX, int sectionY, int sectionZ, boolean playerChanged) {
        int i = Math.floorMod(sectionX, this.gridWidth);
        int j = Math.floorMod(sectionY - this.level.method_32891(), this.gridHeight);
        int k = Math.floorMod(sectionZ, this.gridWidth);
        RenderSection renderSection = this.sections[this.getChunkIndex(i, j, k)];
        renderSection.setDirty(playerChanged);
    }

    @Nullable
    public RenderSection getSectionAtBlockPos(class_2338 blockPos) {
        return this.getSectionAtBlockPos(blockPos.method_10263(), blockPos.method_10264(), blockPos.method_10260());
    }

    public RenderSection getSectionAtBlockPos(int x, int y, int z) {
        int i = x >> 4;
        int j = y - this.level.method_31607() >> 4;
        int k = z >> 4;
        return this.getSectionAtSectionPos(i, j, k);
    }

    public RenderSection getSectionAtSectionPos(int i, int j, int k) {
        if (j >= 0 && j < this.gridHeight) {
            i = Math.floorMod(i, this.gridWidth);
            k = Math.floorMod(k, this.gridWidth);
            return this.sections[this.getChunkIndex(i, j, k)];
        }
        return null;
    }

    public List<RenderSection> getRenderSectionsAt(int x, int z) {
        ObjectArrayList list = new ObjectArrayList(24);
        int i = Math.floorMod(x, this.gridWidth);
        int k = Math.floorMod(z, this.gridWidth);
        for (int y1 = 0; y1 < this.gridHeight; ++y1) {
            list.add((Object)this.sections[this.getChunkIndex(i, y1, k)]);
        }
        return list;
    }

    public void updateFrustumVisibility(VFrustum frustum) {
        this.chunkAreaManager.updateFrustumVisibility(frustum);
    }

    public ChunkAreaManager getChunkAreaManager() {
        return this.chunkAreaManager;
    }

    public int getSectionCount() {
        return this.sections.length;
    }
}

