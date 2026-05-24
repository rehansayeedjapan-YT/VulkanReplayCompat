/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 */
package net.vulkanmod.interfaces.shader;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.vulkanmod.render.engine.EGlProgram;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;

public interface ExtendedRenderPipeline {
    public static ExtendedRenderPipeline of(RenderPipeline renderPipeline) {
        return (ExtendedRenderPipeline)renderPipeline;
    }

    public void setPipeline(GraphicsPipeline var1);

    public void setProgram(EGlProgram var1);

    public GraphicsPipeline getPipeline();

    public EGlProgram getProgram();
}

