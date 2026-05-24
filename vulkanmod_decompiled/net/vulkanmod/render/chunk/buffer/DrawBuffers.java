/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_243
 *  net.minecraft.class_3532
 *  org.joml.Vector3i
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VkCommandBuffer
 */
package net.vulkanmod.render.chunk.buffer;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Iterator;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.vulkanmod.Initializer;
import net.vulkanmod.render.PipelineManager;
import net.vulkanmod.render.chunk.RenderSection;
import net.vulkanmod.render.chunk.buffer.AreaBuffer;
import net.vulkanmod.render.chunk.buffer.DrawParametersBuffer;
import net.vulkanmod.render.chunk.build.UploadBuffer;
import net.vulkanmod.render.chunk.build.task.CompiledSection;
import net.vulkanmod.render.chunk.cull.QuadFacing;
import net.vulkanmod.render.chunk.util.StaticQueue;
import net.vulkanmod.render.vertex.CustomVertexFormat;
import net.vulkanmod.render.vertex.TerrainRenderType;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.memory.MemoryTypes;
import net.vulkanmod.vulkan.memory.buffer.IndirectBuffer;
import net.vulkanmod.vulkan.memory.buffer.UniformBuffer;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.shader.descriptor.UBO;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;

public class DrawBuffers {
    public static final int INDEX_SIZE = 2;
    public static final int UNDEFINED_FACING_IDX = QuadFacing.UNDEFINED.ordinal();
    public static final float POS_OFFSET = CustomVertexFormat.getPositionOffset();
    private static final int CMD_STRIDE = 32;
    private static final long cmdBufferPtr = MemoryUtil.nmemAlignedAlloc((long)32L, (long)(512L * (long)QuadFacing.COUNT * 32L));
    private final int index;
    public final int vertexSize = PipelineManager.getTerrainVertexFormat().getVertexSize();
    private final Vector3i origin;
    private final int minHeight;
    private boolean allocated = false;
    AreaBuffer indexBuffer;
    private final EnumMap<TerrainRenderType, AreaBuffer> vertexBuffers = new EnumMap(TerrainRenderType.class);
    private final UniformBuffer sectionDataBuffer = new UniformBuffer(4096, MemoryTypes.HOST_MEM);
    final long drawParamsPtr;
    final int[] sectionIndices = new int[512];
    final int[] masks = new int[512];
    final long[] buildTimes = new long[512];
    long latestBuildTime = 0L;
    long lastFadeUpdate = -1L;

    public DrawBuffers(int index, Vector3i origin, int minHeight) {
        this.index = index;
        this.origin = origin;
        this.minHeight = minHeight;
        this.drawParamsPtr = DrawParametersBuffer.allocateBuffer();
    }

    public void upload(RenderSection section, UploadBuffer buffer, TerrainRenderType renderType) {
        ByteBuffer[] vertexBuffers = buffer.getVertexBuffers();
        if (buffer.indexOnly) {
            long paramsPtr = DrawParametersBuffer.getParamsPtr(this.drawParamsPtr, section.inAreaIndex, renderType.ordinal(), QuadFacing.UNDEFINED.ordinal());
            int firstIndex = DrawParametersBuffer.getFirstIndex(paramsPtr);
            int indexCount = DrawParametersBuffer.getIndexCount(paramsPtr);
            int oldOffset = indexCount > 0 ? firstIndex : -1;
            AreaBuffer.Segment segment = this.indexBuffer.upload(buffer.getIndexBuffer(), oldOffset, paramsPtr);
            firstIndex = segment.offset / 2;
            DrawParametersBuffer.setFirstIndex(paramsPtr, firstIndex);
            buffer.release();
            return;
        }
        int oldOffset = -1;
        int size = 0;
        for (int i = 0; i < QuadFacing.COUNT; ++i) {
            ByteBuffer vertexBuffer;
            long paramPtr = DrawParametersBuffer.getParamsPtr(this.drawParamsPtr, section.inAreaIndex, renderType.ordinal(), i);
            int vertexOffset = DrawParametersBuffer.getVertexOffset(paramPtr);
            if (oldOffset == -1) {
                oldOffset = vertexOffset;
            }
            if ((vertexBuffer = vertexBuffers[i]) == null) continue;
            size += vertexBuffer.remaining();
        }
        AreaBuffer areaBuffer = null;
        AreaBuffer.Segment segment = null;
        boolean doUpload = false;
        if (size > 0) {
            areaBuffer = this.getAreaBufferOrAlloc(renderType);
            areaBuffer.freeSegment(oldOffset);
            segment = areaBuffer.allocateSegment(size);
            doUpload = true;
        }
        short baseInstance = section.inAreaIndex;
        int offset = 0;
        for (int i = 0; i < QuadFacing.COUNT; ++i) {
            long paramPtr = DrawParametersBuffer.getParamsPtr(this.drawParamsPtr, section.inAreaIndex, renderType.ordinal(), i);
            int vertexOffset = -1;
            int firstIndex = 0;
            int indexCount = 0;
            ByteBuffer vertexBuffer = vertexBuffers[i];
            int vertexCount = 0;
            if (vertexBuffer != null && doUpload) {
                areaBuffer.upload(segment, vertexBuffer, offset);
                vertexOffset = (segment.offset + offset) / this.vertexSize;
                offset += vertexBuffer.remaining();
                vertexCount = vertexBuffer.limit() / this.vertexSize;
                indexCount = vertexCount * 6 / 4;
            }
            if (i == QuadFacing.UNDEFINED.ordinal() && !buffer.autoIndices) {
                if (this.indexBuffer == null) {
                    this.indexBuffer = new AreaBuffer(AreaBuffer.Usage.INDEX, 60000, 2);
                }
                oldOffset = DrawParametersBuffer.getIndexCount(paramPtr) > 0 ? DrawParametersBuffer.getFirstIndex(paramPtr) : -1;
                AreaBuffer.Segment ibSegment = this.indexBuffer.upload(buffer.getIndexBuffer(), oldOffset, paramPtr);
                firstIndex = ibSegment.offset / 2;
            } else {
                Renderer.getDrawer().getQuadsIndexBuffer().checkCapacity(vertexCount);
            }
            DrawParametersBuffer.setIndexCount(paramPtr, indexCount);
            DrawParametersBuffer.setFirstIndex(paramPtr, firstIndex);
            DrawParametersBuffer.setVertexOffset(paramPtr, vertexOffset);
            DrawParametersBuffer.setBaseInstance(paramPtr, baseInstance);
        }
        this.updateUniformData(section);
        buffer.release();
    }

    private void updateUniformData(RenderSection section) {
        int encodedOffset = this.encodeSectionOffset(section.xOffset(), section.yOffset(), section.zOffset());
        int ptrOffset = section.inAreaIndex * 4;
        MemoryUtil.memPutInt((long)(this.sectionDataBuffer.getPointer() + (long)ptrOffset), (int)encodedOffset);
        if (section.getCompiledSection() == CompiledSection.UNCOMPILED) {
            long buildTime;
            this.buildTimes[section.inAreaIndex] = buildTime = System.currentTimeMillis();
            if (buildTime > this.latestBuildTime) {
                this.latestBuildTime = buildTime;
            }
        }
    }

    private void updateFadeUniform(long currentTime, int fadeTimeMs, float fadeTimeInv) {
        if (this.lastFadeUpdate < this.latestBuildTime + (long)fadeTimeMs) {
            int ptrOffset = 2048;
            for (int i = 0; i < 512; ++i) {
                long delta = currentTime - this.buildTimes[i];
                float fade = fadeTimeMs > 0 ? class_3532.method_15363((float)((float)delta * fadeTimeInv), (float)0.0f, (float)1.0f) : 1.0f;
                MemoryUtil.memPutFloat((long)(this.sectionDataBuffer.getPointer() + (long)ptrOffset), (float)fade);
                ptrOffset += 4;
            }
            this.lastFadeUpdate = currentTime;
        }
    }

    private AreaBuffer getAreaBufferOrAlloc(TerrainRenderType renderType) {
        this.allocated = true;
        int initialSize = switch (renderType) {
            default -> throw new MatchException(null, null);
            case TerrainRenderType.SOLID -> 100000;
            case TerrainRenderType.CUTOUT -> 250000;
            case TerrainRenderType.TRANSLUCENT, TerrainRenderType.TRIPWIRE -> 60000;
        };
        return this.vertexBuffers.computeIfAbsent(renderType, renderType1 -> new AreaBuffer(AreaBuffer.Usage.VERTEX, initialSize, this.vertexSize));
    }

    public AreaBuffer getAreaBuffer(TerrainRenderType r) {
        return this.vertexBuffers.get((Object)r);
    }

    private boolean hasRenderType(TerrainRenderType r) {
        return this.vertexBuffers.containsKey((Object)r);
    }

    private int encodeSectionOffset(int xOffset, int yOffset, int zOffset) {
        int xOffset1 = xOffset & 0x7F;
        int zOffset1 = zOffset & 0x7F;
        int yOffset1 = yOffset - this.minHeight & 0x7F;
        return yOffset1 << 16 | zOffset1 << 8 | xOffset1;
    }

    private void updateChunkAreaOrigin(VkCommandBuffer commandBuffer, Pipeline pipeline, double camX, double camY, double camZ, MemoryStack stack) {
        float xOffset = (float)((double)((float)this.origin.x + POS_OFFSET) - camX);
        float yOffset = (float)((double)((float)this.origin.y + POS_OFFSET) - camY);
        float zOffset = (float)((double)((float)this.origin.z + POS_OFFSET) - camZ);
        ByteBuffer byteBuffer = stack.malloc(12);
        byteBuffer.putFloat(0, xOffset);
        byteBuffer.putFloat(4, yOffset);
        byteBuffer.putFloat(8, zOffset);
        VK10.vkCmdPushConstants((VkCommandBuffer)commandBuffer, (long)pipeline.getLayout(), (int)1, (int)0, (ByteBuffer)byteBuffer);
    }

    public void buildDrawBatchesIndirect(class_243 cameraPos, IndirectBuffer indirectBuffer, StaticQueue<RenderSection> queue, TerrainRenderType terrainRenderType) {
        long bufferPtr = cmdBufferPtr;
        boolean isTranslucent = terrainRenderType == TerrainRenderType.TRANSLUCENT;
        boolean backFaceCulling = Initializer.CONFIG.backFaceCulling && !isTranslucent;
        int drawCount = 0;
        long drawParamsBasePtr = this.drawParamsPtr + (long)(terrainRenderType.ordinal() * 512 * 7) * 16L;
        long facingsStride = 112L;
        int count = 0;
        if (backFaceCulling) {
            Iterator<RenderSection> iterator = queue.iterator(isTranslucent);
            while (iterator.hasNext()) {
                RenderSection section = iterator.next();
                this.sectionIndices[count] = section.inAreaIndex;
                this.masks[count] = this.getMask(cameraPos, section);
                ++count;
            }
            long ptr = bufferPtr;
            for (int j = 0; j < count; ++j) {
                int sectionIdx = this.sectionIndices[j];
                int mask = this.masks[j];
                long drawParamsBasePtr2 = drawParamsBasePtr + (long)sectionIdx * 112L;
                int indexCount = 0;
                int firstIndex = 0;
                int vertexOffset = 0;
                int baseInstance = 0;
                for (int i = 0; i < QuadFacing.COUNT; ++i) {
                    if ((mask & 1 << i) == 0) {
                        drawParamsBasePtr2 += 16L;
                        if (indexCount > 0) {
                            MemoryUtil.memPutInt((long)ptr, (int)indexCount);
                            MemoryUtil.memPutInt((long)(ptr + 4L), (int)1);
                            MemoryUtil.memPutInt((long)(ptr + 8L), (int)firstIndex);
                            MemoryUtil.memPutInt((long)(ptr + 12L), (int)vertexOffset);
                            MemoryUtil.memPutInt((long)(ptr + 16L), (int)baseInstance);
                            ptr += 32L;
                            ++drawCount;
                        }
                        indexCount = 0;
                        firstIndex = 0;
                        vertexOffset = 0;
                        baseInstance = 0;
                        continue;
                    }
                    long drawParamsPtr = drawParamsBasePtr2;
                    int indexCount_i = DrawParametersBuffer.getIndexCount(drawParamsPtr);
                    int firstIndex_i = DrawParametersBuffer.getFirstIndex(drawParamsPtr);
                    int vertexOffset_i = DrawParametersBuffer.getVertexOffset(drawParamsPtr);
                    int baseInstance_i = DrawParametersBuffer.getBaseInstance(drawParamsPtr);
                    if (indexCount == 0) {
                        indexCount = indexCount_i;
                        firstIndex = firstIndex_i;
                        vertexOffset = vertexOffset_i;
                        baseInstance = baseInstance_i;
                    } else {
                        indexCount += indexCount_i;
                    }
                    drawParamsBasePtr2 += 16L;
                }
                if (indexCount <= 0) continue;
                MemoryUtil.memPutInt((long)ptr, (int)indexCount);
                MemoryUtil.memPutInt((long)(ptr + 4L), (int)1);
                MemoryUtil.memPutInt((long)(ptr + 8L), (int)firstIndex);
                MemoryUtil.memPutInt((long)(ptr + 12L), (int)vertexOffset);
                MemoryUtil.memPutInt((long)(ptr + 16L), (int)baseInstance);
                ptr += 32L;
                ++drawCount;
            }
        } else {
            Iterator<RenderSection> iterator = queue.iterator(isTranslucent);
            while (iterator.hasNext()) {
                RenderSection section = iterator.next();
                this.sectionIndices[count] = section.inAreaIndex;
                ++count;
            }
            long facingOffset = (long)UNDEFINED_FACING_IDX * 16L;
            drawParamsBasePtr += facingOffset;
            long ptr = bufferPtr;
            for (int i = 0; i < count; ++i) {
                int sectionIdx = this.sectionIndices[i];
                long drawParamsPtr = drawParamsBasePtr + (long)sectionIdx * 112L;
                int indexCount = DrawParametersBuffer.getIndexCount(drawParamsPtr);
                int firstIndex = DrawParametersBuffer.getFirstIndex(drawParamsPtr);
                int vertexOffset = DrawParametersBuffer.getVertexOffset(drawParamsPtr);
                int baseInstance = DrawParametersBuffer.getBaseInstance(drawParamsPtr);
                if (indexCount <= 0) continue;
                MemoryUtil.memPutInt((long)ptr, (int)indexCount);
                MemoryUtil.memPutInt((long)(ptr + 4L), (int)1);
                MemoryUtil.memPutInt((long)(ptr + 8L), (int)firstIndex);
                MemoryUtil.memPutInt((long)(ptr + 12L), (int)vertexOffset);
                MemoryUtil.memPutInt((long)(ptr + 16L), (int)baseInstance);
                ptr += 32L;
                ++drawCount;
            }
        }
        if (drawCount == 0) {
            return;
        }
        ByteBuffer byteBuffer = MemoryUtil.memByteBuffer((long)cmdBufferPtr, (int)(queue.size() * QuadFacing.COUNT * 32));
        indirectBuffer.recordCopyCmd(byteBuffer.position(0));
        VK10.vkCmdDrawIndexedIndirect((VkCommandBuffer)Renderer.getCommandBuffer(), (long)indirectBuffer.getId(), (long)indirectBuffer.getOffset(), (int)drawCount, (int)32);
    }

    public void buildDrawBatchesDirect(class_243 cameraPos, StaticQueue<RenderSection> queue, TerrainRenderType terrainRenderType) {
        boolean isTranslucent = terrainRenderType == TerrainRenderType.TRANSLUCENT;
        boolean backFaceCulling = Initializer.CONFIG.backFaceCulling && !isTranslucent;
        VkCommandBuffer commandBuffer = Renderer.getCommandBuffer();
        long drawParamsBasePtr = this.drawParamsPtr + (long)(terrainRenderType.ordinal() * 512 * 7) * 16L;
        long facingsStride = 112L;
        int count = 0;
        if (backFaceCulling) {
            Iterator<RenderSection> iterator = queue.iterator(isTranslucent);
            while (iterator.hasNext()) {
                RenderSection section = iterator.next();
                this.sectionIndices[count] = section.inAreaIndex;
                this.masks[count] = this.getMask(cameraPos, section);
                ++count;
            }
            for (int j = 0; j < count; ++j) {
                int sectionIdx = this.sectionIndices[j];
                int mask = this.masks[j];
                long drawParamsBasePtr2 = drawParamsBasePtr + (long)sectionIdx * 112L;
                int indexCount = 0;
                int firstIndex = 0;
                int vertexOffset = 0;
                int baseInstance = 0;
                for (int i = 0; i < QuadFacing.COUNT; ++i) {
                    if ((mask & 1 << i) == 0) {
                        drawParamsBasePtr2 += 16L;
                        if (indexCount > 0) {
                            VK10.vkCmdDrawIndexed((VkCommandBuffer)commandBuffer, (int)indexCount, (int)1, (int)firstIndex, (int)vertexOffset, (int)baseInstance);
                        }
                        indexCount = 0;
                        firstIndex = 0;
                        vertexOffset = 0;
                        baseInstance = 0;
                        continue;
                    }
                    long drawParamsPtr = drawParamsBasePtr2;
                    int indexCount_i = DrawParametersBuffer.getIndexCount(drawParamsPtr);
                    int firstIndex_i = DrawParametersBuffer.getFirstIndex(drawParamsPtr);
                    int vertexOffset_i = DrawParametersBuffer.getVertexOffset(drawParamsPtr);
                    int baseInstance_i = DrawParametersBuffer.getBaseInstance(drawParamsPtr);
                    if (indexCount == 0) {
                        indexCount = indexCount_i;
                        firstIndex = firstIndex_i;
                        vertexOffset = vertexOffset_i;
                        baseInstance = baseInstance_i;
                    } else {
                        indexCount += indexCount_i;
                    }
                    drawParamsBasePtr2 += 16L;
                }
                if (indexCount <= 0) continue;
                VK10.vkCmdDrawIndexed((VkCommandBuffer)commandBuffer, (int)indexCount, (int)1, (int)firstIndex, (int)vertexOffset, (int)baseInstance);
            }
        } else {
            long facingOffset = (long)UNDEFINED_FACING_IDX * 16L;
            drawParamsBasePtr += facingOffset;
            Iterator<RenderSection> iterator = queue.iterator(isTranslucent);
            while (iterator.hasNext()) {
                RenderSection section = iterator.next();
                this.sectionIndices[count] = section.inAreaIndex;
                ++count;
            }
            for (int i = 0; i < count; ++i) {
                int sectionIdx = this.sectionIndices[i];
                long drawParamsPtr = drawParamsBasePtr + (long)sectionIdx * 112L;
                int indexCount = DrawParametersBuffer.getIndexCount(drawParamsPtr);
                int firstIndex = DrawParametersBuffer.getFirstIndex(drawParamsPtr);
                int vertexOffset = DrawParametersBuffer.getVertexOffset(drawParamsPtr);
                int baseInstance = DrawParametersBuffer.getBaseInstance(drawParamsPtr);
                if (indexCount <= 0) continue;
                VK10.vkCmdDrawIndexed((VkCommandBuffer)commandBuffer, (int)indexCount, (int)1, (int)firstIndex, (int)vertexOffset, (int)baseInstance);
            }
        }
    }

    private int getMask(class_243 camera, RenderSection section) {
        int secX = section.xOffset;
        int secY = section.yOffset;
        int secZ = section.zOffset;
        int mask = 1 << UNDEFINED_FACING_IDX;
        mask |= camera.field_1352 - (double)secX >= 0.0 ? 1 << QuadFacing.X_POS.ordinal() : 0;
        mask |= camera.field_1351 - (double)secY >= 0.0 ? 1 << QuadFacing.Y_POS.ordinal() : 0;
        mask |= camera.field_1350 - (double)secZ >= 0.0 ? 1 << QuadFacing.Z_POS.ordinal() : 0;
        mask |= camera.field_1352 - (double)(secX + 16) < 0.0 ? 1 << QuadFacing.X_NEG.ordinal() : 0;
        mask |= camera.field_1351 - (double)(secY + 16) < 0.0 ? 1 << QuadFacing.Y_NEG.ordinal() : 0;
        return mask |= camera.field_1350 - (double)(secZ + 16) < 0.0 ? 1 << QuadFacing.Z_NEG.ordinal() : 0;
    }

    public void bindBuffers(VkCommandBuffer commandBuffer, Pipeline pipeline, TerrainRenderType terrainRenderType, double camX, double camY, double camZ, long currentTime, int fadeTimeMs, float fadeTimeInv) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            AreaBuffer vertexBuffer = this.getAreaBuffer(terrainRenderType);
            VK10.nvkCmdBindVertexBuffers((VkCommandBuffer)commandBuffer, (int)0, (int)1, (long)stack.npointer(vertexBuffer.getId()), (long)stack.npointer(0L));
            this.updateChunkAreaOrigin(commandBuffer, pipeline, camX, camY, camZ, stack);
        }
        this.updateFadeUniform(currentTime, fadeTimeMs, fadeTimeInv);
        UBO ubo = pipeline.getUBO(2);
        ubo.setUseGlobalBuffer(false);
        ubo.getBufferSlice().set(this.sectionDataBuffer, 0L, (int)this.sectionDataBuffer.getBufferSize());
        if (terrainRenderType == TerrainRenderType.TRANSLUCENT && this.indexBuffer != null) {
            VK10.vkCmdBindIndexBuffer((VkCommandBuffer)commandBuffer, (long)this.indexBuffer.getId(), (long)0L, (int)0);
        }
    }

    public void releaseBuffers() {
        if (!this.allocated) {
            return;
        }
        this.vertexBuffers.values().forEach(AreaBuffer::freeBuffer);
        this.vertexBuffers.clear();
        if (this.indexBuffer != null) {
            this.indexBuffer.freeBuffer();
        }
        this.indexBuffer = null;
        this.allocated = false;
    }

    public void free() {
        this.releaseBuffers();
        DrawParametersBuffer.freeBuffer(this.drawParamsPtr);
    }

    public boolean isAllocated() {
        return !this.vertexBuffers.isEmpty();
    }

    public EnumMap<TerrainRenderType, AreaBuffer> getVertexBuffers() {
        return this.vertexBuffers;
    }

    public AreaBuffer getIndexBuffer() {
        return this.indexBuffer;
    }

    public long getDrawParamsPtr() {
        return this.drawParamsPtr;
    }
}

