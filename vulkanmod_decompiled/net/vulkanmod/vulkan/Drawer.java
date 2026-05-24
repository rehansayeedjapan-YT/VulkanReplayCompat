/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VkCommandBuffer
 */
package net.vulkanmod.vulkan;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.memory.MemoryTypes;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import net.vulkanmod.vulkan.memory.buffer.IndexBuffer;
import net.vulkanmod.vulkan.memory.buffer.UniformBuffer;
import net.vulkanmod.vulkan.memory.buffer.VertexBuffer;
import net.vulkanmod.vulkan.memory.buffer.index.AutoIndexBuffer;
import net.vulkanmod.vulkan.util.VUtil;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;

public class Drawer {
    private static final int INITIAL_VB_SIZE = 4000000;
    private static final int INITIAL_IB_SIZE = 1000000;
    private static final int INITIAL_UB_SIZE = 200000;
    private static final LongBuffer buffers = MemoryUtil.memAllocLong((int)1);
    private static final LongBuffer offsets = MemoryUtil.memAllocLong((int)1);
    private static final long pBuffers = MemoryUtil.memAddress0((java.nio.Buffer)buffers);
    private static final long pOffsets = MemoryUtil.memAddress0((java.nio.Buffer)offsets);
    private int framesNum;
    private VertexBuffer[] vertexBuffers;
    private IndexBuffer[] indexBuffers;
    private final AutoIndexBuffer quadsIndexBuffer = new AutoIndexBuffer(65536, AutoIndexBuffer.DrawType.QUADS);
    private final AutoIndexBuffer quadsIntIndexBuffer = new AutoIndexBuffer(100000, AutoIndexBuffer.DrawType.QUADS);
    private final AutoIndexBuffer linesIndexBuffer = new AutoIndexBuffer(10000, AutoIndexBuffer.DrawType.LINES);
    private final AutoIndexBuffer debugLineStripIndexBuffer = new AutoIndexBuffer(10000, AutoIndexBuffer.DrawType.DEBUG_LINE_STRIP);
    private final AutoIndexBuffer triangleFanIndexBuffer = new AutoIndexBuffer(1000, AutoIndexBuffer.DrawType.TRIANGLE_FAN);
    private final AutoIndexBuffer triangleStripIndexBuffer = new AutoIndexBuffer(10000, AutoIndexBuffer.DrawType.TRIANGLE_STRIP);
    private UniformBuffer[] uniformBuffers;
    private int currentFrame;

    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }

    public void createResources(int framesNum) {
        this.framesNum = framesNum;
        if (this.vertexBuffers != null) {
            Arrays.stream(this.vertexBuffers).iterator().forEachRemaining(Buffer::scheduleFree);
        }
        this.vertexBuffers = new VertexBuffer[framesNum];
        Arrays.setAll(this.vertexBuffers, i -> new VertexBuffer(4000000, MemoryTypes.HOST_MEM));
        if (this.indexBuffers != null) {
            Arrays.stream(this.indexBuffers).iterator().forEachRemaining(Buffer::scheduleFree);
        }
        this.indexBuffers = new IndexBuffer[framesNum];
        Arrays.setAll(this.indexBuffers, i -> new IndexBuffer(1000000, MemoryTypes.HOST_MEM));
        if (this.uniformBuffers != null) {
            Arrays.stream(this.uniformBuffers).iterator().forEachRemaining(Buffer::scheduleFree);
        }
        this.uniformBuffers = new UniformBuffer[framesNum];
        Arrays.setAll(this.uniformBuffers, i -> new UniformBuffer(200000, MemoryTypes.HOST_MEM));
    }

    public void resetBuffers(int currentFrame) {
        this.vertexBuffers[currentFrame].reset();
        this.indexBuffers[currentFrame].reset();
        this.uniformBuffers[currentFrame].reset();
    }

    public void draw(ByteBuffer vertexData, VertexFormat.class_5596 mode, VertexFormat vertexFormat, int vertexCount) {
        this.draw(vertexData, null, mode, vertexFormat, vertexCount);
    }

    public void draw(ByteBuffer vertexData, ByteBuffer indexData, VertexFormat.class_5596 mode, VertexFormat vertexFormat, int vertexCount) {
        VertexBuffer vertexBuffer = this.vertexBuffers[this.currentFrame];
        int size = vertexFormat.getVertexSize() * vertexCount;
        vertexBuffer.copyBuffer(vertexData, size);
        if (indexData != null) {
            IndexBuffer indexBuffer = this.indexBuffers[this.currentFrame];
            indexBuffer.copyBuffer(indexData, indexData.remaining());
            int indexCount = vertexCount * 3 / 2;
            this.drawIndexed(vertexBuffer, indexBuffer, indexCount);
        } else {
            AutoIndexBuffer autoIndexBuffer = this.getAutoIndexBuffer(mode, vertexCount);
            if (autoIndexBuffer != null) {
                int indexCount = autoIndexBuffer.getIndexCount(vertexCount);
                autoIndexBuffer.checkCapacity(vertexCount);
                this.drawIndexed(vertexBuffer, autoIndexBuffer.getIndexBuffer(), indexCount);
            } else {
                this.draw(vertexBuffer, vertexCount);
            }
        }
    }

    public void drawIndexed(Buffer vertexBuffer, IndexBuffer indexBuffer, int indexCount) {
        this.drawIndexed(vertexBuffer, indexBuffer, indexCount, indexBuffer.indexType.value);
    }

    public void drawIndexed(Buffer vertexBuffer, Buffer indexBuffer, int indexCount, int indexType) {
        VkCommandBuffer commandBuffer = Renderer.getCommandBuffer();
        VUtil.UNSAFE.putLong(pBuffers, vertexBuffer.getId());
        VUtil.UNSAFE.putLong(pOffsets, vertexBuffer.getOffset());
        VK10.nvkCmdBindVertexBuffers((VkCommandBuffer)commandBuffer, (int)0, (int)1, (long)pBuffers, (long)pOffsets);
        this.bindIndexBuffer(commandBuffer, indexBuffer, indexType);
        VK10.vkCmdDrawIndexed((VkCommandBuffer)commandBuffer, (int)indexCount, (int)1, (int)0, (int)0, (int)0);
    }

    public void draw(VertexBuffer vertexBuffer, int vertexCount) {
        VkCommandBuffer commandBuffer = Renderer.getCommandBuffer();
        VUtil.UNSAFE.putLong(pBuffers, vertexBuffer.getId());
        VUtil.UNSAFE.putLong(pOffsets, vertexBuffer.getOffset());
        VK10.nvkCmdBindVertexBuffers((VkCommandBuffer)commandBuffer, (int)0, (int)1, (long)pBuffers, (long)pOffsets);
        VK10.vkCmdDraw((VkCommandBuffer)commandBuffer, (int)vertexCount, (int)1, (int)0, (int)0);
    }

    public void bindIndexBuffer(VkCommandBuffer commandBuffer, Buffer indexBuffer, int indexType) {
        VK10.vkCmdBindIndexBuffer((VkCommandBuffer)commandBuffer, (long)indexBuffer.getId(), (long)indexBuffer.getOffset(), (int)indexType);
    }

    public void cleanUpResources() {
        for (int i = 0; i < this.framesNum; ++i) {
            Buffer buffer = this.vertexBuffers[i];
            MemoryManager.freeBuffer(buffer.getId(), buffer.getAllocation());
            buffer = this.indexBuffers[i];
            MemoryManager.freeBuffer(buffer.getId(), buffer.getAllocation());
            buffer = this.uniformBuffers[i];
            MemoryManager.freeBuffer(buffer.getId(), buffer.getAllocation());
        }
        this.quadsIndexBuffer.freeBuffer();
        this.quadsIntIndexBuffer.freeBuffer();
        this.linesIndexBuffer.freeBuffer();
        this.triangleFanIndexBuffer.freeBuffer();
        this.triangleStripIndexBuffer.freeBuffer();
        this.debugLineStripIndexBuffer.freeBuffer();
    }

    public AutoIndexBuffer getQuadsIndexBuffer() {
        return this.quadsIndexBuffer;
    }

    public AutoIndexBuffer getLinesIndexBuffer() {
        return this.linesIndexBuffer;
    }

    public AutoIndexBuffer getTriangleFanIndexBuffer() {
        return this.triangleFanIndexBuffer;
    }

    public AutoIndexBuffer getTriangleStripIndexBuffer() {
        return this.triangleStripIndexBuffer;
    }

    public AutoIndexBuffer getDebugLineStripIndexBuffer() {
        return this.debugLineStripIndexBuffer;
    }

    public UniformBuffer getUniformBuffer() {
        return this.uniformBuffers[this.currentFrame];
    }

    public AutoIndexBuffer getAutoIndexBuffer(VertexFormat.class_5596 mode, int vertexCount) {
        return switch (mode) {
            default -> throw new MatchException(null, null);
            case VertexFormat.class_5596.field_27382 -> {
                int indexCount = vertexCount * 3 / 2;
                if (indexCount > 65536) {
                    yield this.quadsIntIndexBuffer;
                }
                yield this.quadsIndexBuffer;
            }
            case VertexFormat.class_5596.field_27377 -> this.linesIndexBuffer;
            case VertexFormat.class_5596.field_27381 -> this.triangleFanIndexBuffer;
            case VertexFormat.class_5596.field_27380 -> this.triangleStripIndexBuffer;
            case VertexFormat.class_5596.field_29345 -> this.debugLineStripIndexBuffer;
            case VertexFormat.class_5596.field_63316 -> null;
            case VertexFormat.class_5596.field_27379, VertexFormat.class_5596.field_29344 -> null;
        };
    }
}

