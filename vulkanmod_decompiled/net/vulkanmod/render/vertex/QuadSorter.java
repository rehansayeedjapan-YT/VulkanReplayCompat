/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5595
 *  org.joml.Vector3f
 *  org.lwjgl.system.MemoryUtil
 */
package net.vulkanmod.render.vertex;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.vulkanmod.render.util.SortUtil;
import net.vulkanmod.render.vertex.CustomVertexFormat;
import net.vulkanmod.render.vertex.TerrainBufferBuilder;
import net.vulkanmod.render.vertex.TerrainBuilder;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public class QuadSorter {
    private Vector3f[] sortingPoints;
    private float sortX = Float.NaN;
    private float sortY = Float.NaN;
    private float sortZ = Float.NaN;
    private boolean indexOnly;
    private VertexFormat format;
    private int vertexCount;
    private int indexCount;
    private float[] distances;
    private int[] sortingPointsIndices;

    public void setQuadSortOrigin(float x, float y, float z) {
        this.sortX = x;
        this.sortY = y;
        this.sortZ = z;
    }

    public SortState getSortState() {
        return new SortState(this.vertexCount, this.sortingPoints, this.distances, this.sortingPointsIndices);
    }

    public void restoreSortState(SortState sortState) {
        this.vertexCount = sortState.vertexCount;
        this.sortingPoints = sortState.sortingPoints;
        this.distances = sortState.distances;
        this.sortingPointsIndices = sortState.sortingPointsIndices;
        this.indexOnly = true;
    }

    public void setupQuadSortingPoints(long bufferPtr, int vertexCount, VertexFormat format) {
        this.vertexCount = vertexCount;
        int pointCount = vertexCount / 4;
        Vector3f[] sortingPoints = new Vector3f[pointCount];
        int vertexSize = format.getVertexSize();
        int quadStride = vertexSize * 4;
        int offset = vertexSize * 2;
        if (format == CustomVertexFormat.COMPRESSED_TERRAIN) {
            float invConv = 4.8828125E-4f;
            float convOffset = 4.0f;
            for (int m = 0; m < pointCount; ++m) {
                long ptr = bufferPtr + (long)m * (long)quadStride;
                short x0 = MemoryUtil.memGetShort((long)(ptr + 0L));
                short y0 = MemoryUtil.memGetShort((long)(ptr + 2L));
                short z0 = MemoryUtil.memGetShort((long)(ptr + 4L));
                short x2 = MemoryUtil.memGetShort((long)(ptr + (long)offset + 0L));
                short y2 = MemoryUtil.memGetShort((long)(ptr + (long)offset + 2L));
                short z2 = MemoryUtil.memGetShort((long)(ptr + (long)offset + 4L));
                float xa = (float)(x0 + x2) * 4.8828125E-4f * 0.5f + 4.0f;
                float ya = (float)(y0 + y2) * 4.8828125E-4f * 0.5f + 4.0f;
                float za = (float)(z0 + z2) * 4.8828125E-4f * 0.5f + 4.0f;
                sortingPoints[m] = new Vector3f(xa, ya, za);
            }
        } else {
            for (int m = 0; m < pointCount; ++m) {
                long ptr = bufferPtr + (long)m * (long)quadStride;
                float x0 = MemoryUtil.memGetFloat((long)(ptr + 0L));
                float y0 = MemoryUtil.memGetFloat((long)(ptr + 4L));
                float z0 = MemoryUtil.memGetFloat((long)(ptr + 8L));
                float x2 = MemoryUtil.memGetFloat((long)(ptr + (long)offset + 0L));
                float y2 = MemoryUtil.memGetFloat((long)(ptr + (long)offset + 4L));
                float z2 = MemoryUtil.memGetFloat((long)(ptr + (long)offset + 8L));
                float q = (x0 + x2) * 0.5f;
                float r = (y0 + y2) * 0.5f;
                float s = (z0 + z2) * 0.5f;
                sortingPoints[m] = new Vector3f(q, r, s);
            }
        }
        this.sortingPoints = sortingPoints;
        this.distances = new float[pointCount];
        this.sortingPointsIndices = new int[pointCount];
    }

    public void putSortedQuadIndices(TerrainBufferBuilder bufferBuilder, VertexFormat.class_5595 indexType) {
        float[] distances = this.distances;
        int[] sortingPointsIndices = this.sortingPointsIndices;
        int i = 0;
        while (i < this.sortingPoints.length) {
            float dx = this.sortingPoints[i].x() - this.sortX;
            float dy = this.sortingPoints[i].y() - this.sortY;
            float dz = this.sortingPoints[i].z() - this.sortZ;
            distances[i] = dx * dx + dy * dy + dz * dz;
            sortingPointsIndices[i] = i++;
        }
        SortUtil.mergeSort(sortingPointsIndices, distances);
        long ptr = bufferBuilder.getPtr();
        int size = indexType.field_27375;
        int stride = 4;
        for (int i2 = 0; i2 < sortingPointsIndices.length; ++i2) {
            int quadIndex = sortingPointsIndices[i2];
            int baseVertex = quadIndex * 4;
            MemoryUtil.memPutInt((long)(ptr + (long)size * 0L), (int)(baseVertex + 0));
            MemoryUtil.memPutInt((long)(ptr + (long)size * 1L), (int)(baseVertex + 1));
            MemoryUtil.memPutInt((long)(ptr + (long)size * 2L), (int)(baseVertex + 2));
            MemoryUtil.memPutInt((long)(ptr + (long)size * 3L), (int)(baseVertex + 2));
            MemoryUtil.memPutInt((long)(ptr + (long)size * 4L), (int)(baseVertex + 3));
            MemoryUtil.memPutInt((long)(ptr + (long)size * 5L), (int)(baseVertex + 0));
            ptr += (long)size * 6L;
        }
    }

    public void putSortedQuadIndices(TerrainBuilder bufferBuilder, VertexFormat.class_5595 indexType) {
        float[] distances = new float[this.sortingPoints.length];
        int[] sortingPoints = new int[this.sortingPoints.length];
        int i = 0;
        while (i < this.sortingPoints.length) {
            float dx = this.sortingPoints[i].x() - this.sortX;
            float dy = this.sortingPoints[i].y() - this.sortY;
            float dz = this.sortingPoints[i].z() - this.sortZ;
            distances[i] = dx * dx + dy * dy + dz * dz;
            sortingPoints[i] = i++;
        }
        SortUtil.mergeSort(sortingPoints, distances);
        long ptr = bufferBuilder.indexBufferPtr;
        int size = indexType.field_27375;
        int stride = 4;
        for (int i2 = 0; i2 < sortingPoints.length; ++i2) {
            int quadIndex = sortingPoints[i2];
            int baseVertex = quadIndex * 4;
            MemoryUtil.memPutInt((long)(ptr + (long)size * 0L), (int)(baseVertex + 0));
            MemoryUtil.memPutInt((long)(ptr + (long)size * 1L), (int)(baseVertex + 1));
            MemoryUtil.memPutInt((long)(ptr + (long)size * 2L), (int)(baseVertex + 2));
            MemoryUtil.memPutInt((long)(ptr + (long)size * 3L), (int)(baseVertex + 2));
            MemoryUtil.memPutInt((long)(ptr + (long)size * 4L), (int)(baseVertex + 3));
            MemoryUtil.memPutInt((long)(ptr + (long)size * 5L), (int)(baseVertex + 0));
            ptr += (long)size * 6L;
        }
    }

    public void reset() {
        this.vertexCount = 0;
    }

    public int getVertexCount() {
        return this.vertexCount;
    }

    public int getIndexCount() {
        return this.indexCount;
    }

    public static class SortState {
        final int vertexCount;
        final Vector3f[] sortingPoints;
        final float[] distances;
        final int[] sortingPointsIndices;

        SortState(int vertexCount, Vector3f[] sortingPoints, float[] distances, int[] sortingPointsIndices) {
            this.vertexCount = vertexCount;
            this.sortingPoints = sortingPoints;
            this.distances = distances;
            this.sortingPointsIndices = sortingPointsIndices;
        }
    }
}

