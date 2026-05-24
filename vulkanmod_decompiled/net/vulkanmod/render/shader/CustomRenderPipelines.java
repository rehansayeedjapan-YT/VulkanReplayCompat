/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_10799
 *  net.minecraft.class_290
 */
package net.vulkanmod.render.shader;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_10799;
import net.minecraft.class_290;

public class CustomRenderPipelines {
    public static final List<RenderPipeline> pipelines = new ArrayList<RenderPipeline>();
    public static final RenderPipeline.Snippet GUI_TRIANGLES_SNIPPET = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{class_10799.field_60125}).withVertexShader("core/gui").withFragmentShader("core/gui").withBlend(BlendFunction.TRANSLUCENT).withVertexFormat(class_290.field_1576, VertexFormat.class_5596.field_27379).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).buildSnippet();
    public static final RenderPipeline GUI_TRIANGLES = CustomRenderPipelines.register(RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[]{GUI_TRIANGLES_SNIPPET}).withLocation("pipeline/gui").build());

    static RenderPipeline register(RenderPipeline pipeline) {
        pipelines.add(pipeline);
        return pipeline;
    }
}

