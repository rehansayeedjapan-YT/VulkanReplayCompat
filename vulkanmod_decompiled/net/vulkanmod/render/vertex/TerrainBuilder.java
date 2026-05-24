/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5595
 *  net.minecraft.class_2680
 *  org.apache.logging.log4j.Logger
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.system.MemoryUtil$MemoryAllocator
 */
package net.vulkanmod.render.vertex;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import net.minecraft.class_2680;
import net.vulkanmod.Initializer;
import net.vulkanmod.render.PipelineManager;
import net.vulkanmod.render.chunk.cull.QuadFacing;
import net.vulkanmod.render.vertex.QuadSorter;
import net.vulkanmod.render.vertex.TerrainBufferBuilder;
import net.vulkanmod.render.vertex.VertexBuilder;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryUtil;

public class TerrainBuilder {
    private static final Logger LOGGER = Initializer.LOGGER;
    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator((boolean)false);
    protected long indexBufferPtr;
    private int indexBufferCapacity;
    private final VertexFormat format;
    private boolean building;
    private final QuadSorter quadSorter = new QuadSorter();
    private boolean needsSorting;
    private boolean indexOnly;
    protected VertexBuilder vertexBuilder;
    private final TerrainBufferBuilder[] bufferBuilders;

    public TerrainBuilder(int size, VertexBuilder vertexBuilder) {
        this.indexBufferCapacity = size;
        this.indexBufferPtr = ALLOCATOR.malloc((long)this.indexBufferCapacity);
        this.format = PipelineManager.terrainVertexFormat;
        this.vertexBuilder = vertexBuilder;
        TerrainBufferBuilder[] bufferBuilders = new TerrainBufferBuilder[QuadFacing.COUNT];
        for (int i = 0; i < QuadFacing.COUNT; ++i) {
            bufferBuilders[i] = new TerrainBufferBuilder(size, this.format.getVertexSize(), this.vertexBuilder);
        }
        this.bufferBuilders = bufferBuilders;
    }

    public TerrainBufferBuilder getBufferBuilder(int i) {
        return this.bufferBuilders[i];
    }

    private void ensureIndexCapacity(int size) {
        if (size > this.indexBufferCapacity) {
            int capacity = this.indexBufferCapacity;
            int newSize = (capacity + size) * 2;
            this.resizeIndexBuffer(newSize);
        }
    }

    private void resizeIndexBuffer(int i) {
        this.indexBufferPtr = ALLOCATOR.realloc(this.indexBufferPtr, (long)i);
        LOGGER.debug("Needed to grow index buffer: Old size {} bytes, new size {} bytes.", (Object)this.indexBufferCapacity, (Object)i);
        if (this.indexBufferPtr == 0L) {
            throw new OutOfMemoryError("Failed to resize buffer from " + this.indexBufferCapacity + " bytes to " + i + " bytes");
        }
        this.indexBufferCapacity = i;
    }

    public void setupQuadSorting(float x, float y, float z) {
        this.quadSorter.setQuadSortOrigin(x, y, z);
        this.needsSorting = true;
    }

    public QuadSorter.SortState getSortState() {
        return this.quadSorter.getSortState();
    }

    public void restoreSortState(QuadSorter.SortState sortState) {
        this.quadSorter.restoreSortState(sortState);
        this.indexOnly = true;
    }

    public void setIndexOnly() {
        this.indexOnly = true;
    }

    public void begin() {
        if (this.building) {
            throw new IllegalStateException("Already building!");
        }
        this.building = true;
    }

    public void setupQuadSortingPoints() {
        TerrainBufferBuilder bufferBuilder = this.bufferBuilders[QuadFacing.UNDEFINED.ordinal()];
        long bufferPtr = bufferBuilder.getPtr();
        int vertexCount = bufferBuilder.getVertices();
        this.quadSorter.setupQuadSortingPoints(bufferPtr, vertexCount, this.format);
    }

    public DrawState endDrawing() {
        boolean sequentialIndexing;
        for (TerrainBufferBuilder bufferBuilder : this.bufferBuilders) {
            bufferBuilder.end();
        }
        int vertexCount = this.quadSorter.getVertexCount();
        int indexCount = vertexCount / 4 * 6;
        VertexFormat.class_5595 indexType = VertexFormat.class_5595.method_31972((int)indexCount);
        if (this.needsSorting) {
            int indexBufferSize = indexCount * indexType.field_27375;
            this.ensureIndexCapacity(indexBufferSize);
            this.quadSorter.putSortedQuadIndices(this, indexType);
            sequentialIndexing = false;
        } else {
            sequentialIndexing = true;
        }
        return new DrawState(this.format.getVertexSize(), indexCount, indexType, this.indexOnly, sequentialIndexing);
    }

    public ByteBuffer getIndexBuffer() {
        int indexCount = this.quadSorter.getVertexCount() * 6 / 4;
        return MemoryUtil.memByteBuffer((long)this.indexBufferPtr, (int)(indexCount * 2));
    }

    private void ensureDrawing() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        }
    }

    public void reset() {
        this.building = false;
        this.indexOnly = false;
        this.needsSorting = false;
    }

    public void clear() {
        this.reset();
        for (TerrainBufferBuilder bufferBuilder : this.bufferBuilders) {
            bufferBuilder.clear();
        }
    }

    public void free() {
        ALLOCATOR.free(this.indexBufferPtr);
        for (TerrainBufferBuilder bufferBuilder : this.bufferBuilders) {
            bufferBuilder.free();
        }
    }

    public void setBlockAttributes(class_2680 blockState) {
    }

    public record DrawState(int vertexSize, int indexCount, VertexFormat.class_5595 indexType, boolean indexOnly, boolean sequentialIndex) {
        private int indexBufferSize() {
            return this.sequentialIndex ? 0 : this.indexCount * this.indexType.field_27375;
        }
    }
}

