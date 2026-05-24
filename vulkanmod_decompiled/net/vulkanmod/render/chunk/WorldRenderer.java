/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.blaze3d.opengl.GlStateManager
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  net.minecraft.class_10209
 *  net.minecraft.class_10214
 *  net.minecraft.class_1044
 *  net.minecraft.class_1059
 *  net.minecraft.class_1060
 *  net.minecraft.class_11658
 *  net.minecraft.class_11659
 *  net.minecraft.class_11661
 *  net.minecraft.class_11683$class_11792
 *  net.minecraft.class_11684
 *  net.minecraft.class_11954
 *  net.minecraft.class_12393
 *  net.minecraft.class_1297
 *  net.minecraft.class_1937
 *  net.minecraft.class_2338
 *  net.minecraft.class_243
 *  net.minecraft.class_2586
 *  net.minecraft.class_310
 *  net.minecraft.class_3191
 *  net.minecraft.class_3532
 *  net.minecraft.class_3695
 *  net.minecraft.class_4076
 *  net.minecraft.class_4184
 *  net.minecraft.class_4587
 *  net.minecraft.class_4599
 *  net.minecraft.class_4604
 *  net.minecraft.class_638
 *  net.minecraft.class_824
 *  net.minecraft.class_898
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Matrix4f
 */
package net.vulkanmod.render.chunk;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.textures.GpuTextureView;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import net.minecraft.class_10209;
import net.minecraft.class_10214;
import net.minecraft.class_1044;
import net.minecraft.class_1059;
import net.minecraft.class_1060;
import net.minecraft.class_11658;
import net.minecraft.class_11659;
import net.minecraft.class_11661;
import net.minecraft.class_11683;
import net.minecraft.class_11684;
import net.minecraft.class_11954;
import net.minecraft.class_12393;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_310;
import net.minecraft.class_3191;
import net.minecraft.class_3532;
import net.minecraft.class_3695;
import net.minecraft.class_4076;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4599;
import net.minecraft.class_4604;
import net.minecraft.class_638;
import net.minecraft.class_824;
import net.minecraft.class_898;
import net.vulkanmod.Initializer;
import net.vulkanmod.render.PipelineManager;
import net.vulkanmod.render.chunk.ChunkArea;
import net.vulkanmod.render.chunk.ChunkAreaManager;
import net.vulkanmod.render.chunk.ChunkStatusMap;
import net.vulkanmod.render.chunk.RenderSection;
import net.vulkanmod.render.chunk.SectionGrid;
import net.vulkanmod.render.chunk.buffer.DrawBuffers;
import net.vulkanmod.render.chunk.build.RenderRegionBuilder;
import net.vulkanmod.render.chunk.build.task.ChunkTask;
import net.vulkanmod.render.chunk.build.task.TaskDispatcher;
import net.vulkanmod.render.chunk.graph.SectionGraph;
import net.vulkanmod.render.chunk.util.StaticQueue;
import net.vulkanmod.render.engine.VkGpuTexture;
import net.vulkanmod.render.profiling.BuildTimeProfiler;
import net.vulkanmod.render.profiling.Profiler;
import net.vulkanmod.render.vertex.TerrainRenderType;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.memory.MemoryTypes;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import net.vulkanmod.vulkan.memory.buffer.IndexBuffer;
import net.vulkanmod.vulkan.memory.buffer.IndirectBuffer;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.texture.SamplerManager;
import net.vulkanmod.vulkan.texture.VTextureSelector;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class WorldRenderer {
    private static WorldRenderer INSTANCE;
    public RenderRegionBuilder renderRegionCache;
    private final class_310 minecraft;
    private class_638 level;
    private int renderDistance;
    private final class_4599 renderBuffers;
    private final class_898 entityRenderDispatcher;
    private final class_824 blockEntityRenderDispatcher;
    private final class_11658 levelRenderState;
    private final class_11684 featureRenderDispatcher;
    private float partialTick;
    private class_243 cameraPos;
    private int lastCameraSectionX;
    private int lastCameraSectionY;
    private int lastCameraSectionZ;
    private float lastCameraX;
    private float lastCameraY;
    private float lastCameraZ;
    private float lastCamRotX;
    private float lastCamRotY;
    private SectionGrid sectionGrid;
    private SectionGraph sectionGraph;
    private boolean graphNeedsUpdate;
    private final Set<class_2586> globalBlockEntities = Sets.newHashSet();
    private final TaskDispatcher taskDispatcher;
    private double xTransparentOld;
    private double yTransparentOld;
    private double zTransparentOld;
    IndirectBuffer[] indirectBuffers;
    private long terrainSampler;
    private final List<Runnable> onAllChangedCallbacks = new ObjectArrayList();

    public static WorldRenderer init(class_898 entityRenderDispatcher, class_824 blockEntityRenderDispatcher, class_4599 renderBuffers, class_11658 levelRenderState, class_11684 featureRenderDispatcher) {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        INSTANCE = new WorldRenderer(entityRenderDispatcher, blockEntityRenderDispatcher, renderBuffers, levelRenderState, featureRenderDispatcher);
        return INSTANCE;
    }

    private WorldRenderer(class_898 entityRenderDispatcher, class_824 blockEntityRenderDispatcher, class_4599 renderBuffers, class_11658 levelRenderState, class_11684 featureRenderDispatcher) {
        this.minecraft = class_310.method_1551();
        this.renderBuffers = renderBuffers;
        this.entityRenderDispatcher = entityRenderDispatcher;
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
        this.levelRenderState = levelRenderState;
        this.featureRenderDispatcher = featureRenderDispatcher;
        this.renderRegionCache = new RenderRegionBuilder();
        this.taskDispatcher = new TaskDispatcher();
        ChunkTask.setTaskDispatcher(this.taskDispatcher);
        this.allocateIndirectBuffers();
        TerrainRenderType.updateMapping();
        Renderer.getInstance().addOnResizeCallback(() -> {
            if (this.indirectBuffers.length != Renderer.getFramesNum()) {
                this.allocateIndirectBuffers();
            }
        });
    }

    private void allocateIndirectBuffers() {
        if (this.indirectBuffers != null) {
            Arrays.stream(this.indirectBuffers).forEach(Buffer::scheduleFree);
        }
        this.indirectBuffers = new IndirectBuffer[Renderer.getFramesNum()];
        for (int i = 0; i < this.indirectBuffers.length; ++i) {
            this.indirectBuffers[i] = new IndirectBuffer(1000000, MemoryTypes.HOST_MEM);
        }
    }

    private void benchCallback() {
        BuildTimeProfiler.runBench(this.graphNeedsUpdate || !this.taskDispatcher.isIdle());
    }

    public void setupRenderer(class_4184 camera, class_4604 frustum, boolean isCapturedFrustum, boolean spectator) {
        Profiler profiler = Profiler.getMainProfiler();
        profiler.push("Setup_Renderer");
        class_3695 mcProfiler = class_10209.method_64146();
        this.benchCallback();
        this.cameraPos = camera.method_71156();
        if (this.minecraft.field_1690.method_38521() != this.renderDistance) {
            this.allChanged();
        }
        mcProfiler.method_15396("camera");
        float cameraX = (float)this.cameraPos.method_10216();
        float cameraY = (float)this.cameraPos.method_10214();
        float cameraZ = (float)this.cameraPos.method_10215();
        int sectionX = class_4076.method_32204((double)cameraX);
        int sectionY = class_4076.method_32204((double)cameraY);
        int sectionZ = class_4076.method_32204((double)cameraZ);
        profiler.push("reposition");
        if (this.lastCameraSectionX != sectionX || this.lastCameraSectionY != sectionY || this.lastCameraSectionZ != sectionZ) {
            this.lastCameraSectionX = sectionX;
            this.lastCameraSectionY = sectionY;
            this.lastCameraSectionZ = sectionZ;
            this.sectionGrid.repositionCamera(cameraX, cameraZ);
        }
        profiler.pop();
        double entityDistanceScaling = (Double)this.minecraft.field_1690.method_42517().method_41753();
        class_1297.method_5840((double)(class_3532.method_15350((double)((double)this.renderDistance / 8.0), (double)1.0, (double)2.5) * entityDistanceScaling));
        mcProfiler.method_15405("cull");
        mcProfiler.method_15405("update");
        boolean cameraMoved = false;
        float d_xRot = Math.abs(camera.method_19329() - this.lastCamRotX);
        float d_yRot = Math.abs(camera.method_19330() - this.lastCamRotY);
        cameraMoved |= d_xRot > 2.0f || d_yRot > 2.0f;
        this.graphNeedsUpdate |= (cameraMoved |= cameraX != this.lastCameraX || cameraY != this.lastCameraY || cameraZ != this.lastCameraZ);
        if (!isCapturedFrustum && this.graphNeedsUpdate()) {
            this.graphNeedsUpdate = false;
            this.lastCameraX = cameraX;
            this.lastCameraY = cameraY;
            this.lastCameraZ = cameraZ;
            this.lastCamRotX = camera.method_19329();
            this.lastCamRotY = camera.method_19330();
            this.sectionGraph.update(camera, frustum, spectator);
        }
        this.indirectBuffers[Renderer.getCurrentFrame()].reset();
        mcProfiler.method_15407();
        profiler.pop();
    }

    public void uploadSections() {
        class_3695 mcProfiler = class_10209.method_64146();
        mcProfiler.method_15396("upload");
        Profiler profiler = Profiler.getMainProfiler();
        profiler.push("Uploads");
        try {
            if (this.taskDispatcher.updateSections()) {
                this.graphNeedsUpdate = true;
            }
        }
        catch (Exception e) {
            Initializer.LOGGER.error(e.getMessage());
            this.allChanged();
        }
        profiler.pop();
        mcProfiler.method_15407();
    }

    public boolean isSectionCompiled(class_2338 blockPos) {
        RenderSection renderSection = this.sectionGrid.getSectionAtBlockPos(blockPos);
        return renderSection != null && renderSection.isCompiled();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void allChanged() {
        if (this.level != null) {
            this.level.method_23784();
            this.renderRegionCache.clear();
            this.taskDispatcher.createThreads(Initializer.CONFIG.builderThreads);
            this.graphNeedsUpdate = true;
            this.renderDistance = this.minecraft.field_1690.method_38521();
            if (this.sectionGrid != null) {
                this.sectionGrid.freeAllBuffers();
            }
            this.taskDispatcher.clearBatchQueue();
            Set<class_2586> set = this.globalBlockEntities;
            synchronized (set) {
                this.globalBlockEntities.clear();
            }
            this.sectionGrid = new SectionGrid((class_1937)this.level, this.renderDistance);
            this.sectionGraph = new SectionGraph((class_1937)this.level, this.sectionGrid, this.taskDispatcher);
            this.onAllChangedCallbacks.forEach(Runnable::run);
            class_1297 entity = this.minecraft.method_1560();
            if (entity != null) {
                this.sectionGrid.repositionCamera(entity.method_23317(), entity.method_23321());
            }
        }
    }

    public void setLevel(@Nullable class_638 level) {
        this.lastCameraX = Float.MIN_VALUE;
        this.lastCameraY = Float.MIN_VALUE;
        this.lastCameraZ = Float.MIN_VALUE;
        this.lastCameraSectionX = Integer.MIN_VALUE;
        this.lastCameraSectionY = Integer.MIN_VALUE;
        this.lastCameraSectionZ = Integer.MIN_VALUE;
        this.level = level;
        ChunkStatusMap.createInstance(this.renderDistance);
        if (level != null) {
            this.allChanged();
        } else {
            if (this.sectionGrid != null) {
                this.sectionGrid.freeAllBuffers();
                this.sectionGrid = null;
            }
            this.taskDispatcher.stopThreads();
            this.graphNeedsUpdate = true;
        }
    }

    public void addOnAllChangedCallback(Runnable runnable) {
        this.onAllChangedCallbacks.add(runnable);
    }

    public void clearOnAllChangedCallbacks() {
        this.onAllChangedCallbacks.clear();
    }

    public void renderSectionLayer(TerrainRenderType renderType, double camX, double camY, double camZ, Matrix4f modelView, Matrix4f projection) {
        EnumSet<TerrainRenderType> allowedRenderTypes;
        Renderer.getInstance().getMainPass().rebindMainTarget();
        this.sortTranslucentSections(camX, camY, camZ);
        class_3695 mcProfiler = class_10209.method_64146();
        class_10214 zone = mcProfiler.method_64144(() -> "render_" + String.valueOf((Object)renderType));
        boolean isTranslucent = renderType == TerrainRenderType.TRANSLUCENT;
        boolean indirectDraw = Initializer.CONFIG.indirectDraw;
        if (!isTranslucent) {
            GlStateManager._disableBlend();
        } else {
            GlStateManager._enableBlend();
            VRenderSystem.blendFuncSeparate(770, 771, 1, 771);
        }
        VRenderSystem.enableCull();
        VRenderSystem.depthFunc(515);
        GlStateManager._enableDepthTest();
        GlStateManager._depthMask((boolean)true);
        GlStateManager._colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        GlStateManager._disablePolygonOffset();
        VRenderSystem.setPolygonModeGL(6914);
        VRenderSystem.applyMVP(modelView, projection);
        VRenderSystem.setPrimitiveTopologyGL(4);
        Renderer renderer = Renderer.getInstance();
        GraphicsPipeline pipeline = PipelineManager.getTerrainShader(renderType);
        renderer.bindGraphicsPipeline(pipeline);
        class_1060 textureManager = class_310.method_1551().method_1531();
        class_1044 atlasTexture = textureManager.method_4619(class_1059.field_5275);
        GpuTextureView texView = atlasTexture.method_71659();
        boolean useAnisotropy = this.minecraft.field_1690.method_76747().method_41753() == class_12393.field_64665;
        int maxAnisotropy = this.minecraft.field_1690.method_76248();
        VkGpuTexture texture = (VkGpuTexture)texView.texture();
        if (this.terrainSampler == 0L) {
            this.terrainSampler = SamplerManager.getSampler(true, true, texture.getVulkanImage().mipLevels - 1, useAnisotropy, maxAnisotropy);
        }
        texture.getVulkanImage().setSampler(this.terrainSampler);
        VRenderSystem.setShaderTexture(0, texView);
        VRenderSystem.setShaderTexture(2, class_310.method_1551().field_1773.method_22974().method_71650());
        VTextureSelector.bindShaderTextures(pipeline);
        int atlasTexWidth = texView.getWidth(0);
        int atlasTexHeight = texView.getHeight(0);
        VRenderSystem.setTextureSize(atlasTexWidth, atlasTexHeight);
        VRenderSystem.setCurrentTime((int)System.currentTimeMillis());
        long currentTimeMs = System.currentTimeMillis();
        float fadeTime = ((Double)class_310.method_1551().field_1690.method_76253().method_41753()).floatValue();
        int fadeTimeMs = (int)(fadeTime * 1000.0f);
        float fadeTimeInv = fadeTime > 0.0f ? 1.0f / (fadeTime * 1000.0f) : 1.0f;
        IndexBuffer indexBuffer = Renderer.getDrawer().getQuadsIndexBuffer().getIndexBuffer();
        Renderer.getDrawer().bindIndexBuffer(Renderer.getCommandBuffer(), indexBuffer, indexBuffer.indexType.value);
        int currentFrame = Renderer.getCurrentFrame();
        EnumSet<TerrainRenderType> enumSet = allowedRenderTypes = Initializer.CONFIG.uniqueOpaqueLayer ? TerrainRenderType.COMPACT_RENDER_TYPES : TerrainRenderType.SEMI_COMPACT_RENDER_TYPES;
        if (allowedRenderTypes.contains((Object)renderType)) {
            renderType.setCutoutUniform();
            Iterator<ChunkArea> iterator = this.sectionGraph.getChunkAreaQueue().iterator(isTranslucent);
            while (iterator.hasNext()) {
                ChunkArea chunkArea = iterator.next();
                StaticQueue<RenderSection> queue = chunkArea.sectionQueue;
                DrawBuffers drawBuffers = chunkArea.drawBuffers;
                if (drawBuffers.getAreaBuffer(renderType) == null || queue.size() <= 0) continue;
                drawBuffers.bindBuffers(Renderer.getCommandBuffer(), pipeline, renderType, camX, camY, camZ, currentTimeMs, fadeTimeMs, fadeTimeInv);
                renderer.uploadAndBindUBOs(pipeline);
                if (indirectDraw) {
                    drawBuffers.buildDrawBatchesIndirect(this.cameraPos, this.indirectBuffers[currentFrame], queue, renderType);
                    continue;
                }
                drawBuffers.buildDrawBatchesDirect(this.cameraPos, queue, renderType);
            }
        }
        if (renderType == TerrainRenderType.CUTOUT || renderType == TerrainRenderType.TRIPWIRE) {
            this.indirectBuffers[currentFrame].submitUploads();
        }
        if (!indirectDraw) {
            VRenderSystem.setModelOffset(0.0f, 0.0f, 0.0f);
            renderer.pushConstants(pipeline);
        }
        zone.close();
    }

    private void sortTranslucentSections(double camX, double camY, double camZ) {
        class_3695 mcProfiler = class_10209.method_64146();
        mcProfiler.method_15396("translucent_sort");
        double d0 = camX - this.xTransparentOld;
        double d1 = camY - this.yTransparentOld;
        double d2 = camZ - this.zTransparentOld;
        if (d0 * d0 + d1 * d1 + d2 * d2 > 2.0) {
            this.xTransparentOld = camX;
            this.yTransparentOld = camY;
            this.zTransparentOld = camZ;
            int j = 0;
            Iterator<RenderSection> iterator = this.sectionGraph.getSectionQueue().iterator(false);
            while (iterator.hasNext() && j < 200) {
                RenderSection section = iterator.next();
                section.resortTransparency(this.taskDispatcher);
                if (section.isCompletelyEmpty()) continue;
                ++j;
            }
        }
        mcProfiler.method_15407();
    }

    public void renderBlockEntities(class_4587 poseStack, class_11658 levelRenderState, class_11661 submitNodeStorage, Long2ObjectMap<SortedSet<class_3191>> destructionProgress) {
        Profiler profiler = Profiler.getMainProfiler();
        profiler.pop();
        profiler.push("Block-entities");
        class_243 vec3 = levelRenderState.field_63082.field_63078;
        double camX = vec3.method_10216();
        double camY = vec3.method_10214();
        double camZ = vec3.method_10215();
        for (RenderSection renderSection : this.sectionGraph.getBlockEntitiesSections()) {
            List<class_2586> list = renderSection.getCompiledSection().getBlockEntities();
            if (list.isEmpty()) continue;
            for (class_2586 blockEntity : list) {
                class_11954 blockEntityRenderState;
                class_11683.class_11792 crumblingOverlay;
                class_2338 blockPos = blockEntity.method_11016();
                SortedSet sortedSet = (SortedSet)destructionProgress.get(blockPos.method_10063());
                if (sortedSet != null && !sortedSet.isEmpty()) {
                    poseStack.method_22903();
                    poseStack.method_22904((double)blockPos.method_10263() - camX, (double)blockPos.method_10264() - camY, (double)blockPos.method_10260() - camZ);
                    crumblingOverlay = new class_11683.class_11792(((class_3191)sortedSet.last()).method_13988(), poseStack.method_23760());
                    poseStack.method_22909();
                } else {
                    crumblingOverlay = null;
                }
                if ((blockEntityRenderState = this.blockEntityRenderDispatcher.method_74348(blockEntity, this.partialTick, crumblingOverlay)) == null) continue;
                levelRenderState.field_62646.add(blockEntityRenderState);
            }
        }
        Iterator iterator = this.level.method_72019().iterator();
        while (iterator.hasNext()) {
            class_2586 blockEntity2 = (class_2586)iterator.next();
            if (blockEntity2.method_11015()) {
                iterator.remove();
                continue;
            }
            class_11954 blockEntityRenderState2 = this.blockEntityRenderDispatcher.method_74348(blockEntity2, this.partialTick, null);
            if (blockEntityRenderState2 == null) continue;
            levelRenderState.field_62646.add(blockEntityRenderState2);
        }
        for (class_11954 blockEntityRenderState : levelRenderState.field_62646) {
            class_2338 blockPos = blockEntityRenderState.field_62673;
            poseStack.method_22903();
            poseStack.method_22904((double)blockPos.method_10263() - camX, (double)blockPos.method_10264() - camY, (double)blockPos.method_10260() - camZ);
            class_824 blockEntityRenderDispatcher = this.minecraft.method_31975();
            blockEntityRenderDispatcher.method_3555(blockEntityRenderState, poseStack, (class_11659)submitNodeStorage, levelRenderState.field_63082);
            poseStack.method_22909();
        }
    }

    public void resetSampler() {
        this.terrainSampler = 0L;
    }

    public void setPartialTick(float partialTick) {
        this.partialTick = partialTick;
    }

    public void scheduleGraphUpdate() {
        this.graphNeedsUpdate = true;
    }

    public boolean graphNeedsUpdate() {
        return this.graphNeedsUpdate;
    }

    public int getVisibleSectionsCount() {
        return this.sectionGraph.getSectionQueue().size();
    }

    public void setSectionDirty(int x, int y, int z, boolean flag) {
        this.sectionGrid.setDirty(x, y, z, flag);
        this.renderRegionCache.remove(x, z);
    }

    public SectionGrid getSectionGrid() {
        return this.sectionGrid;
    }

    public ChunkAreaManager getChunkAreaManager() {
        if (this.sectionGrid == null) {
            return null;
        }
        return this.sectionGrid.chunkAreaManager;
    }

    public TaskDispatcher getTaskDispatcher() {
        return this.taskDispatcher;
    }

    public short getLastFrame() {
        return this.sectionGraph.getLastFrame();
    }

    public int getRenderDistance() {
        return this.renderDistance;
    }

    public String getChunkStatistics() {
        if (this.sectionGraph == null) {
            return null;
        }
        return this.sectionGraph.getStatistics();
    }

    public void cleanUp() {
        if (this.indirectBuffers != null) {
            Arrays.stream(this.indirectBuffers).forEach(Buffer::scheduleFree);
        }
    }

    public static WorldRenderer getInstance() {
        return INSTANCE;
    }

    public static class_638 getLevel() {
        return WorldRenderer.INSTANCE.level;
    }

    public static class_243 getCameraPos() {
        return WorldRenderer.INSTANCE.cameraPos;
    }
}

