/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  net.minecraft.class_9801
 *  net.minecraft.class_9801$class_4574
 */
package net.vulkanmod.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import net.minecraft.class_9801;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.memory.MemoryType;
import net.vulkanmod.vulkan.memory.MemoryTypes;
import net.vulkanmod.vulkan.memory.buffer.IndexBuffer;
import net.vulkanmod.vulkan.memory.buffer.VertexBuffer;
import net.vulkanmod.vulkan.memory.buffer.index.AutoIndexBuffer;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.texture.VTextureSelector;

public class VBO {
    private final MemoryType memoryType;
    private VertexBuffer vertexBuffer;
    private IndexBuffer indexBuffer;
    private VertexFormat.class_5596 mode;
    private boolean autoIndexed = false;
    private int indexCount;
    private int vertexCount;

    public VBO(boolean useGpuMem) {
        this.memoryType = useGpuMem ? MemoryTypes.GPU_MEM : MemoryTypes.HOST_MEM;
    }

    public void upload(class_9801 meshData) {
        class_9801.class_4574 parameters = meshData.method_60822();
        this.indexCount = parameters.comp_751();
        this.vertexCount = parameters.comp_750();
        this.mode = parameters.comp_752();
        this.uploadVertexBuffer(parameters, meshData.method_60818());
        this.uploadIndexBuffer(meshData.method_60821());
        meshData.close();
    }

    private void uploadVertexBuffer(class_9801.class_4574 parameters, ByteBuffer data) {
        if (data != null) {
            if (this.vertexBuffer != null) {
                this.vertexBuffer.scheduleFree();
            }
            int size = parameters.comp_749().getVertexSize() * parameters.comp_750();
            this.vertexBuffer = new VertexBuffer(size, this.memoryType);
            this.vertexBuffer.copyBuffer(data, size);
        }
    }

    public void uploadIndexBuffer(ByteBuffer data) {
        if (data == null) {
            AutoIndexBuffer autoIndexBuffer;
            switch (this.mode) {
                case field_27381: {
                    autoIndexBuffer = Renderer.getDrawer().getTriangleFanIndexBuffer();
                    this.indexCount = AutoIndexBuffer.DrawType.getTriangleStripIndexCount(this.vertexCount);
                    break;
                }
                case field_27380: {
                    autoIndexBuffer = Renderer.getDrawer().getTriangleStripIndexBuffer();
                    this.indexCount = AutoIndexBuffer.DrawType.getTriangleStripIndexCount(this.vertexCount);
                    break;
                }
                case field_27382: {
                    autoIndexBuffer = Renderer.getDrawer().getQuadsIndexBuffer();
                    break;
                }
                case field_27377: {
                    autoIndexBuffer = Renderer.getDrawer().getLinesIndexBuffer();
                    break;
                }
                case field_29345: {
                    autoIndexBuffer = Renderer.getDrawer().getDebugLineStripIndexBuffer();
                    break;
                }
                case field_27379: 
                case field_29344: {
                    autoIndexBuffer = null;
                    break;
                }
                default: {
                    throw new IllegalStateException("Unexpected draw mode: %s".formatted(this.mode));
                }
            }
            if (this.indexBuffer != null && !this.autoIndexed) {
                this.indexBuffer.scheduleFree();
            }
            if (autoIndexBuffer != null) {
                autoIndexBuffer.checkCapacity(this.vertexCount);
                this.indexBuffer = autoIndexBuffer.getIndexBuffer();
            }
            this.autoIndexed = true;
        } else {
            if (this.indexBuffer != null && !this.autoIndexed) {
                this.indexBuffer.scheduleFree();
            }
            this.indexBuffer = new IndexBuffer(data.remaining(), MemoryTypes.GPU_MEM);
            this.indexBuffer.copyBuffer(data, data.remaining());
        }
    }

    public void bind(GraphicsPipeline pipeline) {
        Renderer renderer = Renderer.getInstance();
        renderer.bindGraphicsPipeline(pipeline);
        VTextureSelector.bindShaderTextures(pipeline);
        renderer.uploadAndBindUBOs(pipeline);
    }

    public void draw() {
        if (this.indexCount != 0) {
            Renderer renderer = Renderer.getInstance();
            Pipeline pipeline = renderer.getBoundPipeline();
            renderer.uploadAndBindUBOs(pipeline);
            if (this.indexBuffer != null) {
                Renderer.getDrawer().drawIndexed(this.vertexBuffer, this.indexBuffer, this.indexCount);
            } else {
                Renderer.getDrawer().draw(this.vertexBuffer, this.vertexCount);
            }
        }
    }

    public void close() {
        if (this.vertexCount <= 0) {
            return;
        }
        this.vertexBuffer.scheduleFree();
        this.vertexBuffer = null;
        if (!this.autoIndexed) {
            this.indexBuffer.scheduleFree();
            this.indexBuffer = null;
        }
        this.vertexCount = 0;
        this.indexCount = 0;
    }
}

