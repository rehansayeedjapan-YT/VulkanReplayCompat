/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBuffer$MappedView
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.buffers.GpuFence
 *  com.mojang.blaze3d.opengl.GlConst
 *  com.mojang.blaze3d.opengl.GlStateManager
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$UniformDescription
 *  com.mojang.blaze3d.platform.DepthTestFunction
 *  com.mojang.blaze3d.platform.DestFactor
 *  com.mojang.blaze3d.platform.PolygonMode
 *  com.mojang.blaze3d.platform.SourceFactor
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.GpuQuery
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderPass$class_10884
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5595
 *  com.mojang.blaze3d.vertex.VertexFormat$class_5596
 *  com.mojang.logging.LogUtils
 *  net.minecraft.class_1011
 *  net.minecraft.class_1011$class_1012
 *  net.minecraft.class_10866
 *  net.minecraft.class_10868
 *  net.minecraft.class_284
 *  net.minecraft.class_284$class_11272
 *  net.minecraft.class_310
 *  net.minecraft.class_5944
 *  net.minecraft.class_9848
 *  org.jetbrains.annotations.Nullable
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VK11
 *  org.lwjgl.vulkan.VkBufferCopy
 *  org.lwjgl.vulkan.VkBufferCopy$Buffer
 *  org.lwjgl.vulkan.VkBufferMemoryBarrier
 *  org.lwjgl.vulkan.VkBufferMemoryBarrier$Buffer
 *  org.lwjgl.vulkan.VkCommandBuffer
 *  org.lwjgl.vulkan.VkMemoryBarrier
 *  org.lwjgl.vulkan.VkMemoryBarrier$Buffer
 *  org.slf4j.Logger
 */
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuQuery;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Collection;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.class_1011;
import net.minecraft.class_10866;
import net.minecraft.class_10868;
import net.minecraft.class_284;
import net.minecraft.class_310;
import net.minecraft.class_5944;
import net.minecraft.class_9848;
import net.vulkanmod.gl.VkGlFramebuffer;
import net.vulkanmod.gl.VkGlTexture;
import net.vulkanmod.interfaces.shader.ExtendedRenderPipeline;
import net.vulkanmod.render.engine.EGlProgram;
import net.vulkanmod.render.engine.VkFbo;
import net.vulkanmod.render.engine.VkGpuBuffer;
import net.vulkanmod.render.engine.VkGpuDevice;
import net.vulkanmod.render.engine.VkGpuTexture;
import net.vulkanmod.render.engine.VkRenderPass;
import net.vulkanmod.render.engine.VkTextureView;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.Synchronization;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import net.vulkanmod.vulkan.framebuffer.RenderPass;
import net.vulkanmod.vulkan.memory.buffer.StagingBuffer;
import net.vulkanmod.vulkan.memory.buffer.index.AutoIndexBuffer;
import net.vulkanmod.vulkan.queue.CommandPool;
import net.vulkanmod.vulkan.queue.GraphicsQueue;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.descriptor.ImageDescriptor;
import net.vulkanmod.vulkan.shader.descriptor.UBO;
import net.vulkanmod.vulkan.texture.ImageUtil;
import net.vulkanmod.vulkan.texture.VTextureSelector;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferMemoryBarrier;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkMemoryBarrier;
import org.slf4j.Logger;

public class VkCommandEncoder
implements CommandEncoder {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final VkGpuDevice device;
    @Nullable
    private RenderPipeline lastPipeline;
    private boolean inRenderPass;
    @Nullable
    private EGlProgram lastProgram;
    private int framebufferId = VkGlFramebuffer.genFramebufferId();

    protected VkCommandEncoder(VkGpuDevice glDevice) {
        this.device = glDevice;
    }

    public com.mojang.blaze3d.systems.RenderPass createRenderPass(Supplier<String> supplier, GpuTextureView colorAttachmentView, OptionalInt optionalInt) {
        return this.createRenderPass(supplier, colorAttachmentView, optionalInt, null, OptionalDouble.empty());
    }

    public com.mojang.blaze3d.systems.RenderPass createRenderPass(Supplier<String> supplier, GpuTextureView colorAttachmentView, OptionalInt optionalInt, @Nullable GpuTextureView depthTexture, OptionalDouble optionalDouble) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        if (optionalDouble.isPresent() && depthTexture == null) {
            LOGGER.warn("Depth clear value was provided but no depth texture is being used");
        }
        if (class_310.method_1551().method_1522().method_30277() == colorAttachmentView.texture()) {
            Renderer.getInstance().getMainPass().rebindMainTarget();
            int j = 0;
            if (optionalInt.isPresent()) {
                int k = optionalInt.getAsInt();
                GL11.glClearColor((float)class_9848.method_65101((int)k), (float)class_9848.method_65102((int)k), (float)class_9848.method_65103((int)k), (float)class_9848.method_65100((int)k));
                j |= 0x4000;
            }
            if (depthTexture != null && optionalDouble.isPresent()) {
                GL11.glClearDepth((double)optionalDouble.getAsDouble());
                j |= 0x100;
            }
            if (j != 0) {
                GlStateManager._disableScissorTest();
                GlStateManager._depthMask((boolean)true);
                GlStateManager._colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
                GlStateManager._clear((int)j);
            }
            return new VkRenderPass(this, depthTexture != null, true);
        }
        if (colorAttachmentView.isClosed()) {
            throw new IllegalStateException("Color texture is closed");
        }
        if (depthTexture != null && depthTexture.isClosed()) {
            throw new IllegalStateException("Depth texture is closed");
        }
        this.inRenderPass = true;
        GpuTexture depthTexture1 = depthTexture != null ? depthTexture.texture() : null;
        VkFbo fbo = ((VkTextureView)colorAttachmentView).getFbo(depthTexture1);
        fbo.bind();
        int j = 0;
        if (optionalInt.isPresent()) {
            int k = optionalInt.getAsInt();
            GL11.glClearColor((float)class_9848.method_65101((int)k), (float)class_9848.method_65102((int)k), (float)class_9848.method_65103((int)k), (float)class_9848.method_65100((int)k));
            j |= 0x4000;
        }
        if (depthTexture != null && optionalDouble.isPresent()) {
            GL11.glClearDepth((double)optionalDouble.getAsDouble());
            j |= 0x100;
        }
        if (j != 0) {
            GlStateManager._disableScissorTest();
            GlStateManager._depthMask((boolean)true);
            GlStateManager._colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
            GlStateManager._clear((int)j);
        }
        GlStateManager._viewport((int)0, (int)0, (int)colorAttachmentView.getWidth(0), (int)colorAttachmentView.getHeight(0));
        this.lastPipeline = null;
        return new VkRenderPass(this, depthTexture != null, true);
    }

    public void clearColorTexture(GpuTexture colorAttachment, int clearColor) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        if (Renderer.isRecording()) {
            if (class_310.method_1551().method_1522().method_30277() == colorAttachment) {
                Renderer.getInstance().getMainPass().rebindMainTarget();
                VRenderSystem.setClearColor(class_9848.method_65101((int)clearColor), class_9848.method_65102((int)clearColor), class_9848.method_65103((int)clearColor), class_9848.method_65100((int)clearColor));
                Renderer.clearAttachments(16384);
            } else {
                VkGpuTexture vkGpuTexture = (VkGpuTexture)colorAttachment;
                VkGlFramebuffer.bindFramebuffer(36160, this.framebufferId);
                VkGlFramebuffer.framebufferTexture2D(36160, 36064, 3553, vkGpuTexture.method_68427(), 0);
                VkGlFramebuffer.beginRendering(VkGlFramebuffer.getFramebuffer(this.framebufferId));
                VRenderSystem.setClearColor(class_9848.method_65101((int)clearColor), class_9848.method_65102((int)clearColor), class_9848.method_65103((int)clearColor), class_9848.method_65100((int)clearColor));
                Renderer.clearAttachments(16384);
                Renderer.getInstance().endRenderPass();
                VkFbo fbo = ((VkGpuTexture)colorAttachment).getFbo(null);
                ((VkGpuTexture)colorAttachment).setClearColor(clearColor);
                Framebuffer boundFramebuffer = Renderer.getInstance().getBoundFramebuffer();
                if (boundFramebuffer != null && boundFramebuffer.getColorAttachment() == ((VkGpuTexture)colorAttachment).getVulkanImage()) {
                    fbo.clearAttachments();
                }
            }
        } else {
            GraphicsQueue graphicsQueue = DeviceManager.getGraphicsQueue();
            CommandPool.CommandBuffer commandBuffer = graphicsQueue.getCommandBuffer();
            VkGpuTexture vkGpuTexture = (VkGpuTexture)colorAttachment;
            VkGlFramebuffer glFramebuffer = VkGlFramebuffer.getFramebuffer(this.framebufferId);
            glFramebuffer.setAttachmentTexture(36064, vkGpuTexture.method_68427());
            glFramebuffer.create();
            Framebuffer framebuffer = glFramebuffer.getFramebuffer();
            RenderPass renderPass = glFramebuffer.getRenderPass();
            try (MemoryStack stack = MemoryStack.stackPush();){
                framebuffer.beginRenderPass(commandBuffer.handle, renderPass, stack);
            }
            VRenderSystem.setClearColor(class_9848.method_65101((int)clearColor), class_9848.method_65102((int)clearColor), class_9848.method_65103((int)clearColor), class_9848.method_65100((int)clearColor));
            Renderer.clearAttachments(commandBuffer.handle, 16384, 0, 0, framebuffer.getWidth(), framebuffer.getHeight());
            renderPass.endRenderPass(commandBuffer.handle);
            long fence = graphicsQueue.submitCommands(commandBuffer);
            Synchronization.waitFence(fence);
        }
    }

    public void clearColorAndDepthTextures(GpuTexture colorAttachment, int clearColor, GpuTexture depthAttachment, double clearDepth) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        if (class_310.method_1551().method_1522().method_30277() == colorAttachment) {
            Renderer.getInstance().getMainPass().rebindMainTarget();
            VRenderSystem.clearDepth(clearDepth);
            VRenderSystem.setClearColor(class_9848.method_65101((int)clearColor), class_9848.method_65102((int)clearColor), class_9848.method_65103((int)clearColor), class_9848.method_65100((int)clearColor));
            Renderer.clearAttachments(16640);
        } else {
            VkFbo fbo = ((VkGpuTexture)colorAttachment).getFbo(depthAttachment);
            ((VkGpuTexture)colorAttachment).setClearColor(clearColor);
            ((VkGpuTexture)depthAttachment).setDepthClearValue((float)clearDepth);
            Framebuffer boundFramebuffer = Renderer.getInstance().getBoundFramebuffer();
            if (boundFramebuffer != null && boundFramebuffer.getColorAttachment() == ((VkGpuTexture)colorAttachment).getVulkanImage() && boundFramebuffer.getDepthAttachment() == ((VkGpuTexture)depthAttachment).getVulkanImage()) {
                fbo.clearAttachments();
            }
        }
    }

    public void clearColorAndDepthTextures(GpuTexture colorAttachment, int clearColor, GpuTexture depthAttachment, double clearDepth, int x0, int y0, int width, int height) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        VRenderSystem.clearDepth(clearDepth);
        VRenderSystem.setClearColor(class_9848.method_65101((int)clearColor), class_9848.method_65102((int)clearColor), class_9848.method_65103((int)clearColor), class_9848.method_65100((int)clearColor));
        int framebufferHeight = colorAttachment.getHeight(0);
        y0 = framebufferHeight - height - y0;
        Framebuffer boundFramebuffer = Renderer.getInstance().getBoundFramebuffer();
        if (boundFramebuffer != null && boundFramebuffer.getColorAttachment() == ((VkGpuTexture)colorAttachment).getVulkanImage() && boundFramebuffer.getDepthAttachment() == ((VkGpuTexture)depthAttachment).getVulkanImage()) {
            Renderer.clearAttachments(16640, x0, y0, width, height);
        } else {
            VkGpuTexture gpuTexture = (VkGpuTexture)colorAttachment;
            gpuTexture.getFbo(depthAttachment).bind();
            Renderer.clearAttachments(16640, x0, y0, width, height);
        }
    }

    public void clearDepthTexture(GpuTexture depthAttachment, double clearDepth) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        Framebuffer boundFramebuffer = Renderer.getInstance().getBoundFramebuffer();
        if (boundFramebuffer != null && boundFramebuffer.getDepthAttachment() == ((VkGpuTexture)depthAttachment).getVulkanImage()) {
            VRenderSystem.clearDepth(clearDepth);
            Renderer.clearAttachments(256);
        } else {
            ((VkGpuTexture)depthAttachment).setDepthClearValue((float)clearDepth);
        }
    }

    public void writeToBuffer(GpuBufferSlice gpuBufferSlice, ByteBuffer byteBuffer) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        VkGpuBuffer vkGpuBuffer = (VkGpuBuffer)gpuBufferSlice.buffer();
        if (vkGpuBuffer.closed) {
            throw new IllegalStateException("Buffer already closed");
        }
        int size = byteBuffer.remaining();
        if ((long)size + gpuBufferSlice.offset() > vkGpuBuffer.size()) {
            throw new IllegalArgumentException("Cannot write more data than this buffer can hold (attempting to write " + size + " bytes at offset " + gpuBufferSlice.offset() + " to " + gpuBufferSlice.length() + " slice size)");
        }
        long dstOffset = gpuBufferSlice.offset();
        CommandPool.CommandBuffer commandBuffer = Renderer.getInstance().getTransferCb();
        StagingBuffer stagingBuffer = Vulkan.getStagingBuffer();
        stagingBuffer.copyBuffer(size, byteBuffer);
        long srcOffset = stagingBuffer.getOffset();
        try (MemoryStack stack = MemoryStack.stackPush();){
            if (!commandBuffer.isRecording()) {
                commandBuffer.begin(stack);
            }
            VkMemoryBarrier.Buffer barrier = VkMemoryBarrier.calloc((int)1, (MemoryStack)stack);
            barrier.sType$Default();
            VkBufferMemoryBarrier.Buffer bufferMemoryBarriers = VkBufferMemoryBarrier.calloc((int)1, (MemoryStack)stack);
            VkBufferMemoryBarrier bufferMemoryBarrier = (VkBufferMemoryBarrier)bufferMemoryBarriers.get(0);
            bufferMemoryBarrier.sType$Default();
            bufferMemoryBarrier.buffer(vkGpuBuffer.buffer.getId());
            bufferMemoryBarrier.srcAccessMask(4096);
            bufferMemoryBarrier.dstAccessMask(4096);
            bufferMemoryBarrier.size(-1L);
            VK10.vkCmdPipelineBarrier((VkCommandBuffer)commandBuffer.handle, (int)4096, (int)4096, (int)0, (VkMemoryBarrier.Buffer)barrier, (VkBufferMemoryBarrier.Buffer)bufferMemoryBarriers, null);
            VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc((int)1, (MemoryStack)stack);
            copyRegion.size((long)size);
            copyRegion.srcOffset(srcOffset);
            copyRegion.dstOffset(dstOffset);
            VK10.vkCmdCopyBuffer((VkCommandBuffer)commandBuffer.handle, (long)stagingBuffer.getId(), (long)vkGpuBuffer.buffer.getId(), (VkBufferCopy.Buffer)copyRegion);
        }
    }

    public GpuBuffer.MappedView mapBuffer(GpuBuffer gpuBuffer, boolean readable, boolean writable) {
        return this.mapBuffer(gpuBuffer.slice(), readable, writable);
    }

    public GpuBuffer.MappedView mapBuffer(GpuBufferSlice gpuBufferSlice, boolean readable, boolean writable) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        VkGpuBuffer gpuBuffer = (VkGpuBuffer)gpuBufferSlice.buffer();
        if (gpuBuffer.closed) {
            throw new IllegalStateException("Buffer already closed");
        }
        if (!readable && !writable) {
            throw new IllegalArgumentException("At least read or write must be true");
        }
        if (readable && (gpuBuffer.usage() & 1) == 0) {
            throw new IllegalStateException("Buffer is not readable");
        }
        if (writable && (gpuBuffer.usage() & 2) == 0) {
            throw new IllegalStateException("Buffer is not writable");
        }
        if (gpuBufferSlice.offset() + gpuBufferSlice.length() > gpuBuffer.size()) {
            throw new IllegalArgumentException("Cannot map more data than this buffer can hold (attempting to map " + gpuBufferSlice.length() + " bytes at offset " + gpuBufferSlice.offset() + " from " + gpuBuffer.size() + " size buffer)");
        }
        int i = 0;
        if (readable) {
            i |= 1;
        }
        if (writable) {
            i |= 0x22;
        }
        ByteBuffer byteBuffer = MemoryUtil.memByteBuffer((long)(gpuBuffer.getBuffer().getDataPtr() + gpuBufferSlice.offset()), (int)((int)gpuBufferSlice.length()));
        return new VkGpuBuffer.MappedView(0, byteBuffer);
    }

    public void copyToBuffer(GpuBufferSlice gpuBufferSlice, GpuBufferSlice gpuBufferSlice2) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        VkGpuBuffer vkGpuBuffer = (VkGpuBuffer)gpuBufferSlice.buffer();
        if (vkGpuBuffer.closed) {
            throw new IllegalStateException("Source buffer already closed");
        }
        if ((vkGpuBuffer.usage() & 8) == 0) {
            throw new IllegalStateException("Source buffer needs USAGE_COPY_DST to be a destination for a copy");
        }
        VkGpuBuffer vkGpuBuffer2 = (VkGpuBuffer)gpuBufferSlice2.buffer();
        if (vkGpuBuffer2.closed) {
            throw new IllegalStateException("Target buffer already closed");
        }
        if ((vkGpuBuffer2.usage() & 8) == 0) {
            throw new IllegalStateException("Target buffer needs USAGE_COPY_DST to be a destination for a copy");
        }
        if (gpuBufferSlice.length() != gpuBufferSlice2.length()) {
            long var6 = gpuBufferSlice.length();
            throw new IllegalArgumentException("Cannot copy from slice of size " + var6 + " to slice of size " + gpuBufferSlice2.length() + ", they must be equal");
        }
        if (gpuBufferSlice.offset() + gpuBufferSlice.length() > vkGpuBuffer.size()) {
            long var5 = gpuBufferSlice.length();
            throw new IllegalArgumentException("Cannot copy more data than the source buffer holds (attempting to copy " + var5 + " bytes at offset " + gpuBufferSlice.offset() + " from " + vkGpuBuffer.size() + " size buffer)");
        }
        if (gpuBufferSlice2.offset() + gpuBufferSlice2.length() > vkGpuBuffer2.size()) {
            long var10002 = gpuBufferSlice2.length();
            throw new IllegalArgumentException("Cannot copy more data than the target buffer can hold (attempting to copy " + var10002 + " bytes at offset " + gpuBufferSlice2.offset() + " to " + vkGpuBuffer2.size() + " size buffer)");
        }
        throw new UnsupportedOperationException();
    }

    public void writeToTexture(GpuTexture gpuTexture, class_1011 nativeImage) {
        int i = gpuTexture.getWidth(0);
        int j = gpuTexture.getHeight(0);
        if (nativeImage.method_4307() != i || nativeImage.method_4323() != j) {
            throw new IllegalArgumentException("Cannot replace texture of size " + i + "x" + j + " with image of size " + nativeImage.method_4307() + "x" + nativeImage.method_4323());
        }
        if (gpuTexture.isClosed()) {
            throw new IllegalStateException("Destination texture is closed");
        }
        this.writeToTexture(gpuTexture, nativeImage, 0, 0, 0, 0, i, j, 0, 0);
    }

    public void writeToTexture(GpuTexture gpuTexture, class_1011 nativeImage, int level, int arrayLayer, int xOffset, int yOffset, int width, int height, int unpackSkipPixels, int unpackSkipRows) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        if (level >= 0 && level < gpuTexture.getMipLevels()) {
            if (unpackSkipPixels + width > nativeImage.method_4307() || unpackSkipRows + height > nativeImage.method_4323()) {
                throw new IllegalArgumentException("Copy source (" + nativeImage.method_4307() + "x" + nativeImage.method_4323() + ") is not large enough to read a rectangle of " + width + "x" + height + " from " + unpackSkipPixels + "x" + unpackSkipRows);
            }
            if (xOffset + width > gpuTexture.getWidth(level) || yOffset + height > gpuTexture.getHeight(level)) {
                throw new IllegalArgumentException("Dest texture (" + width + "x" + height + ") is not large enough to write a rectangle of " + width + "x" + height + " at " + xOffset + "x" + yOffset + " (at mip level " + level + ")");
            }
            if (gpuTexture.isClosed()) {
                throw new IllegalStateException("Destination texture is closed");
            }
        } else {
            throw new IllegalArgumentException("Invalid mipLevel " + level + ", must be >= 0 and < " + gpuTexture.getMipLevels());
        }
        VTextureSelector.setActiveTexture(0);
        VkGlTexture glTexture = VkGlTexture.getTexture(((class_10868)gpuTexture).method_68427());
        VTextureSelector.bindTexture(glTexture.getVulkanImage());
        VTextureSelector.uploadSubTexture(level, arrayLayer, width, height, xOffset, yOffset, unpackSkipRows, unpackSkipPixels, nativeImage.method_4307(), nativeImage.method_67769());
    }

    public void writeToTexture(GpuTexture gpuTexture, ByteBuffer byteBuffer, class_1011.class_1012 format, int level, int j, int xOffset, int yOffset, int width, int height) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        if (level >= 0 && level < gpuTexture.getMipLevels()) {
            if (width * height * format.method_4335() > byteBuffer.remaining()) {
                throw new IllegalArgumentException("Copy would overrun the source buffer (remaining length of " + byteBuffer.remaining() + ", but copy is " + width + "x" + height + " of format " + String.valueOf(format) + ")");
            }
            if (xOffset + width > gpuTexture.getWidth(level) || yOffset + height > gpuTexture.getHeight(level)) {
                throw new IllegalArgumentException("Dest texture (" + gpuTexture.getWidth(level) + "x" + gpuTexture.getHeight(level) + ") is not large enough to write a rectangle of " + width + "x" + height + " at " + xOffset + "x" + yOffset);
            }
            if (gpuTexture.isClosed()) {
                throw new IllegalStateException("Destination texture is closed");
            }
            if ((gpuTexture.usage() & 1) == 0) {
                throw new IllegalStateException("Color texture must have USAGE_COPY_DST to be a destination for a write");
            }
            if (j >= gpuTexture.getDepthOrLayers()) {
                throw new UnsupportedOperationException("Depth or layer is out of range, must be >= 0 and < " + gpuTexture.getDepthOrLayers());
            }
        } else {
            throw new IllegalArgumentException("Invalid mipLevel, must be >= 0 and < " + gpuTexture.getMipLevels());
        }
        GlStateManager._bindTexture((int)((VkGpuTexture)gpuTexture).id);
        GlStateManager._pixelStore((int)3314, (int)width);
        GlStateManager._pixelStore((int)3316, (int)0);
        GlStateManager._pixelStore((int)3315, (int)0);
        GlStateManager._pixelStore((int)3317, (int)format.method_4335());
        GlStateManager._texSubImage2D((int)3553, (int)level, (int)xOffset, (int)yOffset, (int)width, (int)height, (int)GlConst.toGl((class_1011.class_1012)format), (int)5121, (ByteBuffer)byteBuffer);
    }

    public void copyTextureToBuffer(GpuTexture gpuTexture, GpuBuffer gpuBuffer, long i, Runnable runnable, int j) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        this.copyTextureToBuffer(gpuTexture, gpuBuffer, i, runnable, j, 0, 0, gpuTexture.getWidth(j), gpuTexture.getHeight(j));
    }

    public void copyTextureToBuffer(GpuTexture gpuTexture, GpuBuffer gpuBuffer, long dstOffset, Runnable runnable, int mipLevel, int xOffset, int yOffset, int width, int height) {
        VkGpuBuffer vkGpuBuffer = (VkGpuBuffer)gpuBuffer;
        VkGpuTexture vkGpuTexture = (VkGpuTexture)gpuTexture;
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        if (mipLevel >= 0 && mipLevel < gpuTexture.getMipLevels()) {
            if ((long)(gpuTexture.getWidth(mipLevel) * gpuTexture.getHeight(mipLevel) * vkGpuTexture.getVulkanImage().formatSize) + dstOffset > gpuBuffer.size()) {
                throw new IllegalArgumentException("Buffer of size " + gpuBuffer.size() + " is not large enough to hold " + width + "x" + height + " pixels (" + vkGpuTexture.getVulkanImage().formatSize + " bytes each) starting from offset " + dstOffset);
            }
            if (xOffset + width > gpuTexture.getWidth(mipLevel) || yOffset + height > gpuTexture.getHeight(mipLevel)) {
                throw new IllegalArgumentException("Copy source texture (" + gpuTexture.getWidth(mipLevel) + "x" + gpuTexture.getHeight(mipLevel) + ") is not large enough to read a rectangle of " + width + "x" + height + " from " + xOffset + "," + yOffset);
            }
            if (gpuTexture.isClosed()) {
                throw new IllegalStateException("Source texture is closed");
            }
            if (gpuBuffer.isClosed()) {
                throw new IllegalStateException("Destination buffer is closed");
            }
        } else {
            throw new IllegalArgumentException("Invalid mipLevel " + mipLevel + ", must be >= 0 and < " + gpuTexture.getMipLevels());
        }
        ImageUtil.copyImageToBuffer(vkGpuTexture.getVulkanImage(), vkGpuBuffer.getBuffer(), mipLevel, width, height, xOffset, yOffset, dstOffset, width, height);
        runnable.run();
    }

    public void copyTextureToTexture(GpuTexture gpuTexture, GpuTexture gpuTexture2, int mipLevel, int j, int k, int l, int m, int n, int o) {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        if (mipLevel >= 0 && mipLevel < gpuTexture.getMipLevels() && mipLevel < gpuTexture2.getMipLevels()) {
            if (j + n > gpuTexture2.getWidth(mipLevel) || k + o > gpuTexture2.getHeight(mipLevel)) {
                throw new IllegalArgumentException("Dest texture (" + gpuTexture2.getWidth(mipLevel) + "x" + gpuTexture2.getHeight(mipLevel) + ") is not large enough to write a rectangle of " + n + "x" + o + " at " + j + "x" + k);
            }
            if (l + n > gpuTexture.getWidth(mipLevel) || m + o > gpuTexture.getHeight(mipLevel)) {
                throw new IllegalArgumentException("Source texture (" + gpuTexture.getWidth(mipLevel) + "x" + gpuTexture.getHeight(mipLevel) + ") is not large enough to read a rectangle of " + n + "x" + o + " at " + l + "x" + m);
            }
            if (gpuTexture.isClosed()) {
                throw new IllegalStateException("Source texture is closed");
            }
            if (gpuTexture2.isClosed()) {
                throw new IllegalStateException("Destination texture is closed");
            }
        } else {
            throw new IllegalArgumentException("Invalid mipLevel " + mipLevel + ", must be >= 0 and < " + gpuTexture.getMipLevels() + " and < " + gpuTexture2.getMipLevels());
        }
    }

    public GpuFence createFence() {
        if (this.inRenderPass) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        return new GpuFence(this){

            public void close() {
            }

            public boolean awaitCompletion(long l) {
                return true;
            }
        };
    }

    public GpuQuery timerQueryBegin() {
        return null;
    }

    public void timerQueryEnd(GpuQuery gpuQuery) {
    }

    public void presentTexture(GpuTextureView gpuTexture) {
        throw new UnsupportedOperationException();
    }

    protected <T> void executeDrawMultiple(VkRenderPass renderPass, Collection<RenderPass.class_10884<T>> collection, @Nullable GpuBuffer gpuBuffer, @Nullable VertexFormat.class_5595 indexType, Collection<String> collection2, T object) {
        if (this.trySetup(renderPass)) {
            if (indexType == null) {
                indexType = VertexFormat.class_5595.field_27372;
            }
            GraphicsPipeline pipeline = ExtendedRenderPipeline.of(renderPass.getPipeline()).getPipeline();
            for (RenderPass.class_10884<T> draw : collection) {
                BiConsumer biConsumer;
                VertexFormat.class_5595 indexType2 = draw.comp_3807() == null ? indexType : draw.comp_3807();
                renderPass.setIndexBuffer(draw.comp_3806() == null ? gpuBuffer : draw.comp_3806(), indexType2);
                renderPass.setVertexBuffer(draw.comp_3804(), draw.comp_3805());
                if (class_10866.field_57867) {
                    if (renderPass.indexBuffer == null) {
                        throw new IllegalStateException("Missing index buffer");
                    }
                    if (renderPass.indexBuffer.isClosed()) {
                        throw new IllegalStateException("Index buffer has been closed!");
                    }
                    if (renderPass.vertexBuffers[0] == null) {
                        throw new IllegalStateException("Missing vertex buffer at slot 0");
                    }
                    if (renderPass.vertexBuffers[0].isClosed()) {
                        throw new IllegalStateException("Vertex buffer at slot 0 has been closed!");
                    }
                }
                if ((biConsumer = draw.comp_3810()) != null) {
                    biConsumer.accept(object, (string, gpuBufferSlice) -> {
                        EGlProgram glProgram = ExtendedRenderPipeline.of(renderPass.pipeline).getProgram();
                        class_284 patt0$temp = glProgram.getUniform(string);
                        if (patt0$temp instanceof class_284.class_11272) {
                            class_284.class_11272 ubo = (class_284.class_11272)patt0$temp;
                            try {
                                int blockBinding = ubo.comp_4150();
                            }
                            catch (Throwable var7) {
                                throw new MatchException(var7.toString(), var7);
                            }
                        }
                    });
                    Renderer.getInstance().uploadAndBindUBOs(pipeline);
                }
                this.drawFromBuffers(renderPass, 0, draw.comp_3808(), draw.comp_3809(), indexType2, renderPass.pipeline, 1);
            }
        }
    }

    protected void executeDraw(VkRenderPass renderPass, int vertexOffset, int firstIndex, int vertexCount, @Nullable VertexFormat.class_5595 indexType, int instanceCount) {
        if (this.trySetup(renderPass)) {
            if (class_10866.field_57867) {
                if (indexType != null) {
                    if (renderPass.indexBuffer == null) {
                        throw new IllegalStateException("Missing index buffer");
                    }
                    if (renderPass.indexBuffer.isClosed()) {
                        throw new IllegalStateException("Index buffer has been closed!");
                    }
                }
                if (renderPass.vertexBuffers[0] == null) {
                    throw new IllegalStateException("Missing vertex buffer at slot 0");
                }
                if (renderPass.vertexBuffers[0].isClosed()) {
                    throw new IllegalStateException("Vertex buffer at slot 0 has been closed!");
                }
            }
            this.drawFromBuffers(renderPass, vertexOffset, firstIndex, vertexCount, indexType, renderPass.pipeline, instanceCount);
        }
    }

    public void drawFromBuffers(VkRenderPass renderPass, int vertexOffset, int firstIndex, int vertexCount, @Nullable VertexFormat.class_5595 indexType, RenderPipeline renderPipeline, int instanceCount) {
        if (instanceCount < 1) {
            instanceCount = 1;
        }
        if (vertexOffset < 0) {
            vertexOffset = 0;
        }
        VkCommandBuffer vkCommandBuffer = Renderer.getCommandBuffer();
        VkGpuBuffer vertexBuffer = (VkGpuBuffer)renderPass.vertexBuffers[0];
        try (MemoryStack stack = MemoryStack.stackPush();){
            if (vertexBuffer != null) {
                VK11.vkCmdBindVertexBuffers((VkCommandBuffer)vkCommandBuffer, (int)0, (LongBuffer)stack.longs(vertexBuffer.buffer.getId()), (LongBuffer)stack.longs(0L));
            }
            if (renderPass.indexBuffer != null) {
                VkGpuBuffer indexBuffer = (VkGpuBuffer)renderPass.indexBuffer;
                int vkIndexType = switch (indexType) {
                    default -> throw new MatchException(null, null);
                    case VertexFormat.class_5595.field_27372 -> 0;
                    case VertexFormat.class_5595.field_27373 -> 1;
                };
                VK11.vkCmdBindIndexBuffer((VkCommandBuffer)vkCommandBuffer, (long)indexBuffer.buffer.getId(), (long)0L, (int)vkIndexType);
                VK11.vkCmdDrawIndexed((VkCommandBuffer)vkCommandBuffer, (int)vertexCount, (int)instanceCount, (int)firstIndex, (int)vertexOffset, (int)0);
            } else {
                AutoIndexBuffer autoIndexBuffer = Renderer.getDrawer().getAutoIndexBuffer(renderPipeline.getVertexFormatMode(), vertexCount);
                if (autoIndexBuffer != null) {
                    int indexCount = autoIndexBuffer.getIndexCount(vertexCount);
                    VK11.vkCmdBindIndexBuffer((VkCommandBuffer)vkCommandBuffer, (long)autoIndexBuffer.getIndexBuffer().getId(), (long)0L, (int)autoIndexBuffer.getIndexBuffer().indexType.value);
                    VK11.vkCmdDrawIndexed((VkCommandBuffer)vkCommandBuffer, (int)indexCount, (int)instanceCount, (int)firstIndex, (int)vertexOffset, (int)0);
                } else {
                    VK11.vkCmdDraw((VkCommandBuffer)vkCommandBuffer, (int)vertexCount, (int)instanceCount, (int)vertexOffset, (int)0);
                }
            }
        }
    }

    public boolean trySetup(VkRenderPass renderPass) {
        if (VkRenderPass.VALIDATION) {
            if (renderPass.pipeline == null) {
                throw new IllegalStateException("Can't draw without a render pipeline");
            }
            for (RenderPipeline.UniformDescription uniformDescription : renderPass.pipeline.getUniforms()) {
                GpuBufferSlice object = renderPass.uniforms.get(uniformDescription.name());
                if (object != null || class_5944.field_57863.contains(uniformDescription.name())) continue;
                throw new IllegalStateException("Missing uniform " + uniformDescription.name() + " (should be " + String.valueOf(uniformDescription.type()) + ")");
            }
        }
        this.applyPipelineState(renderPass.pipeline);
        this.setupUniforms(renderPass);
        if (renderPass.isScissorEnabled()) {
            GlStateManager._enableScissorTest();
            GlStateManager._scissorBox((int)renderPass.getScissorX(), (int)renderPass.getScissorY(), (int)renderPass.getScissorWidth(), (int)renderPass.getScissorHeight());
        } else {
            GlStateManager._disableScissorTest();
        }
        return this.bindPipeline(renderPass.pipeline);
    }

    public void setupUniforms(VkRenderPass renderPass) {
        String uniformName;
        RenderPipeline renderPipeline = renderPass.pipeline;
        EGlProgram glProgram = ExtendedRenderPipeline.of(renderPass.pipeline).getProgram();
        GraphicsPipeline pipeline = ExtendedRenderPipeline.of(renderPass.pipeline).getPipeline();
        for (UBO ubo : pipeline.getBuffers()) {
            uniformName = ubo.name;
            class_284 uniform = glProgram.getUniform(uniformName);
            GpuBufferSlice gpuBufferSlice = renderPass.uniforms.get(uniformName);
            if (gpuBufferSlice == null) {
                ubo.setUseGlobalBuffer(true);
                ubo.setUpdate(true);
                continue;
            }
            VkGpuBuffer gpuBuffer = (VkGpuBuffer)gpuBufferSlice.buffer();
            assert (ubo != null);
            ubo.setUseGlobalBuffer(false);
            ubo.getBufferSlice().set(gpuBuffer.buffer, gpuBufferSlice.offset(), (int)gpuBufferSlice.length());
        }
        for (ImageDescriptor imageDescriptor : pipeline.getImageDescriptors()) {
            VkTextureView textureView;
            VkGpuTexture gpuTexture;
            uniformName = imageDescriptor.name;
            int samplerIndex = imageDescriptor.imageIdx;
            VkRenderPass.TextureViewAndSampler textureSampler = renderPass.samplers.get(uniformName);
            if (textureSampler == null || (gpuTexture = (textureView = textureSampler.view()).texture()).isClosed()) continue;
            GlStateManager._activeTexture((int)(33984 + samplerIndex));
            GlStateManager._bindTexture((int)gpuTexture.id);
            gpuTexture.getVulkanImage().setSampler(textureSampler.sampler().getId());
        }
    }

    public boolean bindPipeline(RenderPipeline renderPipeline) {
        GraphicsPipeline pipeline = ExtendedRenderPipeline.of(renderPipeline).getPipeline();
        if (pipeline == null) {
            return false;
        }
        Renderer renderer = Renderer.getInstance();
        renderer.bindGraphicsPipeline(pipeline);
        renderer.uploadAndBindUBOs(pipeline);
        return true;
    }

    public void applyPipelineState(RenderPipeline renderPipeline) {
        if (this.lastPipeline != renderPipeline) {
            this.lastPipeline = renderPipeline;
            if (renderPipeline.getDepthTestFunction() != DepthTestFunction.NO_DEPTH_TEST) {
                GlStateManager._enableDepthTest();
                GlStateManager._depthFunc((int)GlConst.toGl((DepthTestFunction)renderPipeline.getDepthTestFunction()));
            } else {
                GlStateManager._disableDepthTest();
            }
            if (renderPipeline.isCull()) {
                GlStateManager._enableCull();
            } else {
                GlStateManager._disableCull();
            }
            if (renderPipeline.getBlendFunction().isPresent()) {
                GlStateManager._enableBlend();
                BlendFunction blendFunction = (BlendFunction)renderPipeline.getBlendFunction().get();
                GlStateManager._blendFuncSeparate((int)GlConst.toGl((SourceFactor)blendFunction.sourceColor()), (int)GlConst.toGl((DestFactor)blendFunction.destColor()), (int)GlConst.toGl((SourceFactor)blendFunction.sourceAlpha()), (int)GlConst.toGl((DestFactor)blendFunction.destAlpha()));
            } else {
                GlStateManager._disableBlend();
            }
            GlStateManager._polygonMode((int)1032, (int)GlConst.toGl((PolygonMode)renderPipeline.getPolygonMode()));
            GlStateManager._depthMask((boolean)renderPipeline.isWriteDepth());
            GlStateManager._colorMask((boolean)renderPipeline.isWriteColor(), (boolean)renderPipeline.isWriteColor(), (boolean)renderPipeline.isWriteColor(), (boolean)renderPipeline.isWriteAlpha());
            if (renderPipeline.getDepthBiasConstant() == 0.0f && renderPipeline.getDepthBiasScaleFactor() == 0.0f) {
                GlStateManager._disablePolygonOffset();
            } else {
                GlStateManager._polygonOffset((float)renderPipeline.getDepthBiasScaleFactor(), (float)renderPipeline.getDepthBiasConstant());
                GlStateManager._enablePolygonOffset();
            }
            switch (renderPipeline.getColorLogic()) {
                case NONE: {
                    GlStateManager._disableColorLogicOp();
                    break;
                }
                case OR_REVERSE: {
                    GlStateManager._enableColorLogicOp();
                    GlStateManager._logicOp((int)5387);
                }
            }
            VRenderSystem.setPrimitiveTopologyGL(GlConst.toGl((VertexFormat.class_5596)renderPipeline.getVertexFormatMode()));
        }
    }

    public void finishRenderPass(boolean forceEnd) {
        if (forceEnd) {
            Renderer.getInstance().endRenderPass();
        }
        this.inRenderPass = false;
    }

    protected VkGpuDevice getDevice() {
        return this.device;
    }
}

