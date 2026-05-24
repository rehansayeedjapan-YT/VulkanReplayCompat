/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderPass$class_10884
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5595
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_11219
 *  net.minecraft.class_12137
 *  net.minecraft.class_155
 *  org.jetbrains.annotations.Nullable
 */
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11219;
import net.minecraft.class_12137;
import net.minecraft.class_155;
import net.vulkanmod.interfaces.shader.ExtendedRenderPipeline;
import net.vulkanmod.render.engine.VkCommandEncoder;
import net.vulkanmod.render.engine.VkSampler;
import net.vulkanmod.render.engine.VkTextureView;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class VkRenderPass
implements RenderPass {
    protected static final int MAX_VERTEX_BUFFERS = 1;
    public static final boolean VALIDATION = class_155.field_1125;
    private final VkCommandEncoder encoder;
    private final boolean hasDepthTexture;
    private boolean closed;
    @Nullable
    protected RenderPipeline pipeline;
    protected final GpuBuffer[] vertexBuffers = new GpuBuffer[1];
    @Nullable
    protected GpuBuffer indexBuffer;
    protected VertexFormat.class_5595 indexType = VertexFormat.class_5595.field_27373;
    private final class_11219 scissorState = new class_11219();
    protected final HashMap<String, GpuBufferSlice> uniforms = new HashMap();
    protected final HashMap<String, TextureViewAndSampler> samplers = new HashMap();
    protected final Set<String> dirtyUniforms = new HashSet<String>();
    protected int pushedDebugGroups;
    private final boolean autoManaged;

    public VkRenderPass(VkCommandEncoder commandEncoder, boolean hasDepthTexture, boolean autoManaged) {
        this.encoder = commandEncoder;
        this.hasDepthTexture = hasDepthTexture;
        this.autoManaged = autoManaged;
    }

    public boolean hasDepthTexture() {
        return this.hasDepthTexture;
    }

    public void pushDebugGroup(Supplier<String> supplier) {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        ++this.pushedDebugGroups;
    }

    public void popDebugGroup() {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        if (this.pushedDebugGroups == 0) {
            throw new IllegalStateException("Can't pop more debug groups than was pushed!");
        }
        --this.pushedDebugGroups;
    }

    public void setPipeline(RenderPipeline renderPipeline) {
        if (this.pipeline == null || this.pipeline != renderPipeline) {
            this.dirtyUniforms.addAll(this.uniforms.keySet());
        }
        this.pipeline = renderPipeline;
        if (ExtendedRenderPipeline.of(renderPipeline).getPipeline() == null) {
            this.encoder.getDevice().compilePipeline(renderPipeline);
        }
    }

    public void bindTexture(String string, @Nullable GpuTextureView gpuTextureView, @Nullable class_12137 gpuSampler) {
        if (gpuSampler == null) {
            this.samplers.remove(string);
        } else {
            this.samplers.put(string, new TextureViewAndSampler((VkTextureView)gpuTextureView, (VkSampler)gpuSampler));
        }
        this.dirtyUniforms.add(string);
    }

    public void setUniform(String string, GpuBuffer gpuBuffer) {
        this.uniforms.put(string, gpuBuffer.slice());
        this.dirtyUniforms.add(string);
    }

    public void setUniform(String string, GpuBufferSlice gpuBufferSlice) {
        int i = this.encoder.getDevice().getUniformOffsetAlignment();
        if (gpuBufferSlice.offset() % (long)i > 0L) {
            throw new IllegalArgumentException("Uniform buffer offset must be aligned to " + i);
        }
        this.uniforms.put(string, gpuBufferSlice);
        this.dirtyUniforms.add(string);
    }

    public void enableScissor(int i, int j, int k, int l) {
        this.scissorState.method_70814(i, j, k, l);
    }

    public void disableScissor() {
        this.scissorState.method_70813();
    }

    public boolean isScissorEnabled() {
        return this.scissorState.method_72091();
    }

    public int getScissorX() {
        return this.scissorState.method_72092();
    }

    public int getScissorY() {
        return this.scissorState.method_72093();
    }

    public int getScissorWidth() {
        return this.scissorState.method_72094();
    }

    public int getScissorHeight() {
        return this.scissorState.method_72095();
    }

    public class_11219 getScissorState() {
        return this.scissorState;
    }

    public void setVertexBuffer(int i, GpuBuffer gpuBuffer) {
        if (i < 0 || i >= 1) {
            throw new IllegalArgumentException("Vertex buffer slot is out of range: " + i);
        }
        this.vertexBuffers[i] = gpuBuffer;
    }

    public void setIndexBuffer(@Nullable GpuBuffer gpuBuffer, VertexFormat.class_5595 indexType) {
        this.indexBuffer = gpuBuffer;
        this.indexType = indexType;
    }

    public void drawIndexed(int vertexOffset, int firstIndex, int vertexCount, int instanceCount) {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        this.encoder.executeDraw(this, vertexOffset, firstIndex, vertexCount, this.indexType, instanceCount);
    }

    public <T> void drawMultipleIndexed(Collection<RenderPass.class_10884<T>> collection, @Nullable GpuBuffer gpuBuffer, @Nullable VertexFormat.class_5595 indexType, Collection<String> collection2, T object) {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        this.encoder.executeDrawMultiple(this, collection, gpuBuffer, indexType, collection2, object);
    }

    public void draw(int vertexOffset, int vertexCount) {
        if (this.closed) {
            throw new IllegalStateException("Can't use a closed render pass");
        }
        this.encoder.executeDraw(this, vertexOffset, 0, vertexCount, null, 1);
    }

    public void close() {
        if (!this.closed) {
            if (this.pushedDebugGroups > 0) {
                throw new IllegalStateException("Render pass had debug groups left open!");
            }
            this.closed = true;
            this.encoder.finishRenderPass(!this.autoManaged);
        }
    }

    @Nullable
    public RenderPipeline getPipeline() {
        return this.pipeline;
    }

    @Environment(value=EnvType.CLIENT)
    protected record TextureViewAndSampler(VkTextureView view, VkSampler sampler) {
    }
}

