/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  net.minecraft.class_1921
 *  net.minecraft.class_290
 */
package net.vulkanmod.render;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Function;
import net.minecraft.class_1921;
import net.minecraft.class_290;
import net.vulkanmod.render.chunk.build.thread.ThreadBuilderPack;
import net.vulkanmod.render.shader.ShaderLoadUtil;
import net.vulkanmod.render.vertex.CustomVertexFormat;
import net.vulkanmod.render.vertex.TerrainRenderType;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.shader.descriptor.UBO;

public abstract class PipelineManager {
    public static VertexFormat terrainVertexFormat;
    static GraphicsPipeline terrainShader;
    static GraphicsPipeline terrainShaderEarlyZ;
    static GraphicsPipeline fastBlitPipeline;
    static GraphicsPipeline cloudsPipeline;
    private static Function<TerrainRenderType, GraphicsPipeline> shaderGetter;

    public static void init() {
        PipelineManager.setTerrainVertexFormat(CustomVertexFormat.COMPRESSED_TERRAIN);
        PipelineManager.createBasicPipelines();
        PipelineManager.setDefaultTerrainShaderGetter();
        ThreadBuilderPack.defaultTerrainBuilderConstructor();
    }

    public static void setDefaultTerrainShaderGetter() {
        PipelineManager.setShaderGetter(renderType -> terrainShader);
    }

    private static void createBasicPipelines() {
        terrainShaderEarlyZ = PipelineManager.createPipeline("terrain_earlyZ", CustomVertexFormat.COMPRESSED_TERRAIN);
        terrainShader = PipelineManager.createPipeline("terrain", CustomVertexFormat.COMPRESSED_TERRAIN);
        fastBlitPipeline = PipelineManager.createPipeline("blit", CustomVertexFormat.NONE);
        cloudsPipeline = PipelineManager.createPipeline("clouds", class_290.field_1576);
    }

    private static GraphicsPipeline createPipeline(String configName, VertexFormat vertexFormat) {
        Pipeline.Builder pipelineBuilder = new Pipeline.Builder(vertexFormat, configName);
        String path = ShaderLoadUtil.resolveShaderPath("basic");
        JsonObject config = ShaderLoadUtil.getJsonConfig(path, configName);
        pipelineBuilder.parseBindings(config);
        ShaderLoadUtil.loadShaders(pipelineBuilder, config, configName, path);
        GraphicsPipeline pipeline = pipelineBuilder.createGraphicsPipeline();
        for (UBO buffer : pipeline.getBuffers()) {
            buffer.setUseGlobalBuffer(true);
        }
        return pipeline;
    }

    public static GraphicsPipeline getTerrainShader(TerrainRenderType renderType) {
        return shaderGetter.apply(renderType);
    }

    public static void setShaderGetter(Function<TerrainRenderType, GraphicsPipeline> consumer) {
        shaderGetter = consumer;
    }

    public static void setTerrainVertexFormat(VertexFormat format) {
        terrainVertexFormat = format;
    }

    public static VertexFormat getTerrainVertexFormat() {
        return terrainVertexFormat;
    }

    public static GraphicsPipeline getTerrainDirectShader(class_1921 renderType) {
        return terrainShader;
    }

    public static GraphicsPipeline getTerrainIndirectShader(class_1921 renderType) {
        return terrainShaderEarlyZ;
    }

    public static GraphicsPipeline getFastBlitPipeline() {
        return fastBlitPipeline;
    }

    public static GraphicsPipeline getCloudsPipeline() {
        return cloudsPipeline;
    }

    public static void destroyPipelines() {
        terrainShaderEarlyZ.cleanUp();
        terrainShader.cleanUp();
        fastBlitPipeline.cleanUp();
        cloudsPipeline.cleanUp();
    }
}

