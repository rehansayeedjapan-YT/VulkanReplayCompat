/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.opengl.GlStateManager
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  net.minecraft.class_310
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.system.Pointer
 *  org.lwjgl.vulkan.KHRSwapchain
 *  org.lwjgl.vulkan.VK10
 *  org.lwjgl.vulkan.VkClearAttachment
 *  org.lwjgl.vulkan.VkClearAttachment$Buffer
 *  org.lwjgl.vulkan.VkClearRect
 *  org.lwjgl.vulkan.VkClearRect$Buffer
 *  org.lwjgl.vulkan.VkClearValue
 *  org.lwjgl.vulkan.VkCommandBuffer
 *  org.lwjgl.vulkan.VkCommandBufferAllocateInfo
 *  org.lwjgl.vulkan.VkCommandBufferBeginInfo
 *  org.lwjgl.vulkan.VkDevice
 *  org.lwjgl.vulkan.VkFenceCreateInfo
 *  org.lwjgl.vulkan.VkPresentInfoKHR
 *  org.lwjgl.vulkan.VkQueue
 *  org.lwjgl.vulkan.VkRect2D
 *  org.lwjgl.vulkan.VkRect2D$Buffer
 *  org.lwjgl.vulkan.VkSemaphoreCreateInfo
 *  org.lwjgl.vulkan.VkSubmitInfo
 *  org.lwjgl.vulkan.VkViewport
 *  org.lwjgl.vulkan.VkViewport$Buffer
 */
package net.vulkanmod.vulkan;

import com.mojang.blaze3d.opengl.GlStateManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.class_310;
import net.vulkanmod.Initializer;
import net.vulkanmod.gl.VkGlFramebuffer;
import net.vulkanmod.mixin.window.WindowAccessor;
import net.vulkanmod.render.PipelineManager;
import net.vulkanmod.render.chunk.WorldRenderer;
import net.vulkanmod.render.chunk.buffer.UploadManager;
import net.vulkanmod.render.profiling.Profiler;
import net.vulkanmod.render.texture.ImageUploadHelper;
import net.vulkanmod.vulkan.Drawer;
import net.vulkanmod.vulkan.Synchronization;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import net.vulkanmod.vulkan.framebuffer.RenderPass;
import net.vulkanmod.vulkan.framebuffer.SwapChain;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.pass.DefaultMainPass;
import net.vulkanmod.vulkan.pass.MainPass;
import net.vulkanmod.vulkan.queue.CommandPool;
import net.vulkanmod.vulkan.queue.TransferQueue;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.shader.PipelineState;
import net.vulkanmod.vulkan.shader.Uniforms;
import net.vulkanmod.vulkan.shader.layout.PushConstants;
import net.vulkanmod.vulkan.texture.VTextureSelector;
import net.vulkanmod.vulkan.util.VkResult;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkClearAttachment;
import org.lwjgl.vulkan.VkClearRect;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkViewport;

public class Renderer {
    private static Renderer INSTANCE;
    private static VkDevice device;
    private static boolean swapChainUpdate;
    private final Set<Pipeline> usedPipelines = new ObjectOpenHashSet();
    private Pipeline boundPipeline;
    private long boundPipelineHandle;
    private Drawer drawer;
    private SwapChain swapChain;
    private int framesNum;
    private List<VkCommandBuffer> mainCommandBuffers;
    private ArrayList<Long> imageAvailableSemaphores;
    private ArrayList<Long> renderFinishedSemaphores;
    private ArrayList<Long> inFlightFences;
    private List<CommandPool.CommandBuffer> transferCbs;
    private Framebuffer boundFramebuffer;
    private RenderPass boundRenderPass;
    private static int currentFrame;
    private static int imageIndex;
    private static int lastReset;
    private VkCommandBuffer currentCmdBuffer;
    private boolean recordingCmds = false;
    int recursion = 0;
    MainPass mainPass;
    private final List<Runnable> onResizeCallbacks = new ObjectArrayList();

    public static void initRenderer() {
        INSTANCE = new Renderer();
        INSTANCE.init();
    }

    public static Renderer getInstance() {
        return INSTANCE;
    }

    public static Drawer getDrawer() {
        return Renderer.INSTANCE.drawer;
    }

    public static int getCurrentFrame() {
        return currentFrame;
    }

    public static int getCurrentImage() {
        return imageIndex;
    }

    public Renderer() {
        device = Vulkan.getVkDevice();
        this.framesNum = Initializer.CONFIG.frameQueueSize;
    }

    public static void setLineWidth(float width) {
        if (Renderer.INSTANCE.boundFramebuffer == null) {
            return;
        }
        VK10.vkCmdSetLineWidth((VkCommandBuffer)Renderer.INSTANCE.currentCmdBuffer, (float)width);
    }

    private void init() {
        MemoryManager.createInstance(Renderer.getFramesNum());
        Vulkan.createStagingBuffers();
        this.swapChain = new SwapChain();
        this.mainPass = DefaultMainPass.create();
        this.drawer = new Drawer();
        this.drawer.createResources(this.framesNum);
        Uniforms.setupDefaultUniforms();
        PipelineManager.init();
        UploadManager.createInstance();
        this.allocateCommandBuffers();
        this.createSyncObjects();
    }

    private void allocateCommandBuffers() {
        if (this.mainCommandBuffers != null) {
            this.mainCommandBuffers.forEach(commandBuffer -> VK10.vkFreeCommandBuffers((VkDevice)device, (long)Vulkan.getCommandPool(), (VkCommandBuffer)commandBuffer));
        }
        this.mainCommandBuffers = new ArrayList<VkCommandBuffer>(this.framesNum);
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc((MemoryStack)stack);
            allocInfo.sType(40);
            allocInfo.commandPool(Vulkan.getCommandPool());
            allocInfo.level(0);
            allocInfo.commandBufferCount(this.framesNum);
            PointerBuffer pCommandBuffers = stack.mallocPointer(this.framesNum);
            int vkResult = VK10.vkAllocateCommandBuffers((VkDevice)device, (VkCommandBufferAllocateInfo)allocInfo, (PointerBuffer)pCommandBuffers);
            if (vkResult != 0) {
                throw new RuntimeException("Failed to allocate command buffers: %s".formatted(VkResult.decode(vkResult)));
            }
            for (int i = 0; i < this.framesNum; ++i) {
                this.mainCommandBuffers.add(new VkCommandBuffer(pCommandBuffers.get(i), device));
            }
        }
        if (this.transferCbs != null) {
            this.transferCbs.forEach(commandBuffer -> {
                VK10.vkResetCommandBuffer((VkCommandBuffer)commandBuffer.handle, (int)0);
                commandBuffer.reset();
            });
        }
        this.transferCbs = new ArrayList<CommandPool.CommandBuffer>(this.framesNum);
        for (int i = 0; i < this.framesNum; ++i) {
            this.transferCbs.add(DeviceManager.getTransferQueue().getCommandPool().getCommandBuffer());
        }
    }

    private void createSyncObjects() {
        int swapChainImages = this.swapChain.getImagesNum();
        this.renderFinishedSemaphores = new ArrayList(swapChainImages);
        this.imageAvailableSemaphores = new ArrayList(this.framesNum);
        this.inFlightFences = new ArrayList(this.framesNum);
        try (MemoryStack stack = MemoryStack.stackPush();){
            int i;
            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc((MemoryStack)stack);
            semaphoreInfo.sType(9);
            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc((MemoryStack)stack);
            fenceInfo.sType(8);
            fenceInfo.flags(1);
            LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
            LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);
            LongBuffer pFence = stack.mallocLong(1);
            for (i = 0; i < this.framesNum; ++i) {
                if (VK10.vkCreateSemaphore((VkDevice)device, (VkSemaphoreCreateInfo)semaphoreInfo, null, (LongBuffer)pImageAvailableSemaphore) != 0 || VK10.vkCreateFence((VkDevice)device, (VkFenceCreateInfo)fenceInfo, null, (LongBuffer)pFence) != 0) {
                    throw new RuntimeException("Failed to create synchronization objects for the frame: " + i);
                }
                this.imageAvailableSemaphores.add(pImageAvailableSemaphore.get(0));
                this.inFlightFences.add(pFence.get(0));
            }
            for (i = 0; i < this.swapChain.getImagesNum(); ++i) {
                if (VK10.vkCreateSemaphore((VkDevice)device, (VkSemaphoreCreateInfo)semaphoreInfo, null, (LongBuffer)pRenderFinishedSemaphore) != 0) {
                    throw new RuntimeException("Failed to create synchronization objects for the image: " + i);
                }
                this.renderFinishedSemaphores.add(pRenderFinishedSemaphore.get(0));
            }
        }
    }

    public void preInitFrame() {
        Profiler p = Profiler.getMainProfiler();
        p.pop();
        p.round();
        p.push("Frame_ops");
        if (lastReset == currentFrame) {
            this.submitUploads();
            this.waitFences();
        }
        lastReset = currentFrame;
        this.drawer.resetBuffers(currentFrame);
        WorldRenderer.getInstance().uploadSections();
        UploadManager.INSTANCE.submitUploads();
    }

    public void beginFrame() {
        if (swapChainUpdate) {
            this.recreateSwapChain();
            swapChainUpdate = false;
        }
        ++this.recursion;
        if (this.recursion > 1) {
            this.endFrame();
        }
        this.preInitFrame();
        Profiler p = Profiler.getMainProfiler();
        p.pop();
        p.push("Frame_fence");
        VK10.vkWaitForFences((VkDevice)device, (long)this.inFlightFences.get(currentFrame), (boolean)true, (long)-1L);
        p.pop();
        p.push("Begin_rendering");
        MemoryManager.getInstance().initFrame(currentFrame);
        this.drawer.setCurrentFrame(currentFrame);
        this.resetDescriptors();
        this.currentCmdBuffer = this.mainCommandBuffers.get(currentFrame);
        VK10.vkResetCommandBuffer((VkCommandBuffer)this.currentCmdBuffer, (int)0);
        try (MemoryStack stack = MemoryStack.stackPush();){
            if (this.swapChain.hasImages()) {
                IntBuffer pImageIndex = stack.mallocInt(1);
                long semaphore = this.imageAvailableSemaphores.get(currentFrame);
                int vkResult = KHRSwapchain.vkAcquireNextImageKHR((VkDevice)device, (long)this.swapChain.getId(), (long)-1L, (long)semaphore, (long)0L, (IntBuffer)pImageIndex);
                if (vkResult == 1000001003 || vkResult == -1000001004 || swapChainUpdate) {
                    swapChainUpdate = true;
                    this.beginFrame();
                    return;
                }
                if (vkResult != 0) {
                    throw new RuntimeException("Cannot acquire next swap chain image: %s".formatted(VkResult.decode(vkResult)));
                }
                imageIndex = pImageIndex.get(0);
            }
            this.beginMainRenderPass(stack);
        }
        p.pop();
    }

    private void beginMainRenderPass(MemoryStack stack) {
        VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc((MemoryStack)stack);
        beginInfo.sType(42);
        beginInfo.flags(1);
        VkCommandBuffer commandBuffer = this.currentCmdBuffer;
        int vkResult = VK10.vkBeginCommandBuffer((VkCommandBuffer)commandBuffer, (VkCommandBufferBeginInfo)beginInfo);
        if (vkResult != 0) {
            throw new RuntimeException("Failed to begin recording command buffer: %s".formatted(VkResult.decode(vkResult)));
        }
        this.recordingCmds = true;
        this.mainPass.begin(commandBuffer, stack);
        Renderer.resetDynamicState(commandBuffer);
    }

    public void endFrame() {
        if (!this.recordingCmds) {
            return;
        }
        if (this.recursion == 0) {
            return;
        }
        --this.recursion;
        Profiler p = Profiler.getMainProfiler();
        p.push("End_rendering");
        this.mainPass.end(this.currentCmdBuffer);
        this.submitUploads();
        this.waitFences();
        this.submitFrame();
        this.recordingCmds = false;
        this.boundRenderPass = null;
        this.boundFramebuffer = null;
        p.pop();
        p.push("Post_rendering");
    }

    private void submitFrame() {
        if (swapChainUpdate || !this.swapChain.hasImages()) {
            try (MemoryStack stack = MemoryStack.stackPush();){
                VkSubmitInfo submitInfo = VkSubmitInfo.calloc((MemoryStack)stack);
                submitInfo.sType(4);
                LongBuffer waitSemaphores = Synchronization.INSTANCE.getWaitSemaphores(stack);
                int waitSemaphoreCount = waitSemaphores.limit();
                IntBuffer waitDstStageMask = stack.mallocInt(waitSemaphoreCount);
                for (int i = 0; i < waitSemaphoreCount; ++i) {
                    waitDstStageMask.put(i, 1);
                }
                submitInfo.pWaitSemaphores(waitSemaphores);
                submitInfo.waitSemaphoreCount(waitSemaphores.limit());
                submitInfo.pWaitDstStageMask(waitDstStageMask);
                submitInfo.pCommandBuffers(stack.pointers((Pointer)this.currentCmdBuffer));
                VK10.vkResetFences((VkDevice)device, (long)this.inFlightFences.get(currentFrame));
                int vkResult = VK10.vkQueueSubmit((VkQueue)DeviceManager.getGraphicsQueue().vkQueue(), (VkSubmitInfo)submitInfo, (long)this.inFlightFences.get(currentFrame));
                if (vkResult != 0) {
                    VK10.vkResetFences((VkDevice)device, (long)this.inFlightFences.get(currentFrame));
                    throw new RuntimeException("Failed to submit draw command buffer: %s".formatted(VkResult.decode(vkResult)));
                }
                Synchronization.INSTANCE.scheduleCbReset();
            }
            currentFrame = (currentFrame + 1) % this.framesNum;
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkSubmitInfo submitInfo = VkSubmitInfo.calloc((MemoryStack)stack);
            submitInfo.sType(4);
            Synchronization.INSTANCE.addWaitSemaphore(this.imageAvailableSemaphores.get(currentFrame));
            LongBuffer waitSemaphores = Synchronization.INSTANCE.getWaitSemaphores(stack);
            int waitSemaphoreCount = waitSemaphores.limit();
            IntBuffer waitDstStageMask = stack.mallocInt(waitSemaphoreCount);
            for (int i = 0; i < waitSemaphoreCount - 1; ++i) {
                waitDstStageMask.put(i, 1);
            }
            waitDstStageMask.put(waitSemaphoreCount - 1, 1024);
            submitInfo.pWaitSemaphores(waitSemaphores);
            submitInfo.waitSemaphoreCount(waitSemaphores.limit());
            submitInfo.pWaitDstStageMask(waitDstStageMask);
            submitInfo.pSignalSemaphores(stack.longs(this.renderFinishedSemaphores.get(imageIndex).longValue()));
            submitInfo.pCommandBuffers(stack.pointers((Pointer)this.currentCmdBuffer));
            VK10.vkResetFences((VkDevice)device, (long)this.inFlightFences.get(currentFrame));
            int vkResult = VK10.vkQueueSubmit((VkQueue)DeviceManager.getGraphicsQueue().vkQueue(), (VkSubmitInfo)submitInfo, (long)this.inFlightFences.get(currentFrame));
            if (vkResult != 0) {
                VK10.vkResetFences((VkDevice)device, (long)this.inFlightFences.get(currentFrame));
                throw new RuntimeException("Failed to submit draw command buffer: %s".formatted(VkResult.decode(vkResult)));
            }
            Synchronization.INSTANCE.scheduleCbReset();
            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc((MemoryStack)stack);
            presentInfo.sType(1000001001);
            presentInfo.pWaitSemaphores(stack.longs(this.renderFinishedSemaphores.get(imageIndex).longValue()));
            presentInfo.swapchainCount(1);
            presentInfo.pSwapchains(stack.longs(this.swapChain.getId()));
            presentInfo.pImageIndices(stack.ints(imageIndex));
            vkResult = KHRSwapchain.vkQueuePresentKHR((VkQueue)DeviceManager.getPresentQueue().vkQueue(), (VkPresentInfoKHR)presentInfo);
            if (vkResult == -1000001004 || vkResult == 1000001003 || swapChainUpdate) {
                swapChainUpdate = true;
                return;
            }
            if (vkResult != 0) {
                throw new RuntimeException("Failed to present rendered frame: %s".formatted(VkResult.decode(vkResult)));
            }
            currentFrame = (currentFrame + 1) % this.framesNum;
        }
    }

    public void flushCmds() {
        if (!this.recordingCmds) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush();){
            this.endRenderPass(this.currentCmdBuffer);
            VK10.vkEndCommandBuffer((VkCommandBuffer)this.currentCmdBuffer);
            VkSubmitInfo submitInfo = VkSubmitInfo.calloc((MemoryStack)stack);
            submitInfo.sType(4);
            submitInfo.pCommandBuffers(stack.pointers((Pointer)this.currentCmdBuffer));
            LongBuffer waitSemaphores = Synchronization.INSTANCE.getWaitSemaphores(stack);
            int waitSemaphoreCount = waitSemaphores.limit();
            IntBuffer waitDstStageMask = stack.mallocInt(waitSemaphoreCount);
            for (int i = 0; i < waitSemaphoreCount; ++i) {
                waitDstStageMask.put(i, 1);
            }
            submitInfo.pWaitSemaphores(waitSemaphores);
            submitInfo.waitSemaphoreCount(waitSemaphores.limit());
            submitInfo.pWaitDstStageMask(waitDstStageMask);
            this.submitUploads();
            this.waitFences();
            VK10.vkResetFences((VkDevice)device, (long)this.inFlightFences.get(currentFrame));
            int vkResult = VK10.vkQueueSubmit((VkQueue)DeviceManager.getGraphicsQueue().vkQueue(), (VkSubmitInfo)submitInfo, (long)this.inFlightFences.get(currentFrame));
            if (vkResult != 0) {
                VK10.vkResetFences((VkDevice)device, (long)this.inFlightFences.get(currentFrame));
                throw new RuntimeException("Failed to submit draw command buffer: %s".formatted(VkResult.decode(vkResult)));
            }
            VK10.vkWaitForFences((VkDevice)device, (long)this.inFlightFences.get(currentFrame), (boolean)true, (long)-1L);
            this.beginMainRenderPass(stack);
        }
    }

    public void submitUploads() {
        CommandPool.CommandBuffer transferCb = this.transferCbs.get(currentFrame);
        if (transferCb.isRecording()) {
            TransferQueue transferQueue = DeviceManager.getTransferQueue();
            try (MemoryStack stack = MemoryStack.stackPush();){
                transferCb.submitCommands(stack, transferQueue.vkQueue(), true);
            }
            Synchronization.INSTANCE.addCommandBuffer(transferCb, true);
            this.transferCbs.set(currentFrame, transferQueue.getCommandPool().getCommandBuffer());
        }
        ImageUploadHelper.INSTANCE.submitCommands();
    }

    public void endRenderPass() {
        this.endRenderPass(this.currentCmdBuffer);
    }

    public void endRenderPass(VkCommandBuffer commandBuffer) {
        if (!this.recordingCmds || this.boundFramebuffer == null) {
            return;
        }
        this.boundRenderPass.endRenderPass(commandBuffer);
        this.boundRenderPass = null;
        this.boundFramebuffer = null;
        VkGlFramebuffer.resetBoundFramebuffer();
    }

    public boolean beginRenderPass(RenderPass renderPass, Framebuffer framebuffer) {
        if (!this.recordingCmds) {
            this.beginFrame();
            this.recordingCmds = true;
        }
        if (this.boundFramebuffer != framebuffer) {
            this.endRenderPass(this.currentCmdBuffer);
            try (MemoryStack stack = MemoryStack.stackPush();){
                framebuffer.beginRenderPass(this.currentCmdBuffer, renderPass, stack);
            }
            this.boundFramebuffer = framebuffer;
            this.boundRenderPass = renderPass;
            Renderer.setViewportState(0, 0, framebuffer.getWidth(), framebuffer.getHeight());
            Renderer.setScissor(0, 0, framebuffer.getWidth(), framebuffer.getHeight());
        }
        return true;
    }

    public void addUsedPipeline(Pipeline pipeline) {
        this.usedPipelines.add(pipeline);
    }

    public void removeUsedPipeline(Pipeline pipeline) {
        this.usedPipelines.remove(pipeline);
    }

    private void waitFences() {
        Synchronization.INSTANCE.waitFences();
        Vulkan.getStagingBuffer().reset();
    }

    private void resetDescriptors() {
        for (Pipeline pipeline : this.usedPipelines) {
            pipeline.resetDescriptorPool(currentFrame);
        }
        this.usedPipelines.clear();
        this.boundPipeline = null;
        this.boundPipelineHandle = 0L;
    }

    void waitForSwapChain() {
        VK10.vkResetFences((VkDevice)device, (long)this.inFlightFences.get(currentFrame));
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkSubmitInfo info = VkSubmitInfo.calloc((MemoryStack)stack).sType$Default().pWaitSemaphores(stack.longs(this.imageAvailableSemaphores.get(currentFrame).longValue())).pWaitDstStageMask(stack.ints(65536));
            VK10.vkQueueSubmit((VkQueue)DeviceManager.getGraphicsQueue().vkQueue(), (VkSubmitInfo)info, (long)this.inFlightFences.get(currentFrame));
            VK10.vkWaitForFences((VkDevice)device, (long)this.inFlightFences.get(currentFrame), (boolean)true, (long)-1L);
        }
    }

    private void recreateSwapChain() {
        this.submitUploads();
        this.waitFences();
        Vulkan.waitIdle();
        this.mainCommandBuffers.forEach(commandBuffer -> VK10.vkResetCommandBuffer((VkCommandBuffer)commandBuffer, (int)0));
        this.recordingCmds = false;
        this.swapChain.recreate();
        this.destroySyncObjects();
        int newFramesNum = Initializer.CONFIG.frameQueueSize;
        if (this.framesNum != newFramesNum) {
            UploadManager.INSTANCE.submitUploads();
            this.framesNum = newFramesNum;
            MemoryManager.getInstance().freeAllBuffers();
            MemoryManager.createInstance(newFramesNum);
            Vulkan.createStagingBuffers();
            this.allocateCommandBuffers();
            Pipeline.recreateDescriptorSets(this.framesNum);
            this.drawer.createResources(this.framesNum);
        }
        this.createSyncObjects();
        this.mainPass.onResize();
        this.onResizeCallbacks.forEach(Runnable::run);
        ((WindowAccessor)class_310.method_1551().method_22683()).getEventHandler().method_15993();
        currentFrame = 0;
    }

    public void cleanUpResources() {
        WorldRenderer.getInstance().cleanUp();
        this.destroySyncObjects();
        this.drawer.cleanUpResources();
        this.mainPass.cleanUp();
        this.swapChain.cleanUp();
        PipelineManager.destroyPipelines();
        VTextureSelector.getWhiteTexture().free();
    }

    private void destroySyncObjects() {
        int i;
        for (i = 0; i < this.framesNum; ++i) {
            VK10.vkDestroyFence((VkDevice)device, (long)this.inFlightFences.get(i), null);
            VK10.vkDestroySemaphore((VkDevice)device, (long)this.imageAvailableSemaphores.get(i), null);
        }
        for (i = 0; i < this.swapChain.getImagesNum(); ++i) {
            VK10.vkDestroySemaphore((VkDevice)device, (long)this.renderFinishedSemaphores.get(i), null);
        }
    }

    public void addOnResizeCallback(Runnable runnable) {
        this.onResizeCallbacks.add(runnable);
    }

    public void bindGraphicsPipeline(GraphicsPipeline pipeline) {
        VkCommandBuffer commandBuffer = this.currentCmdBuffer;
        PipelineState currentState = PipelineState.getCurrentPipelineState(this.boundRenderPass);
        long handle = pipeline.getHandle(currentState);
        if (this.boundPipelineHandle == handle) {
            return;
        }
        VK10.vkCmdBindPipeline((VkCommandBuffer)commandBuffer, (int)0, (long)handle);
        this.boundPipelineHandle = handle;
        this.boundPipeline = pipeline;
        this.addUsedPipeline(pipeline);
    }

    public void uploadAndBindUBOs(Pipeline pipeline) {
        VkCommandBuffer commandBuffer = this.currentCmdBuffer;
        pipeline.bindDescriptorSets(commandBuffer, currentFrame);
    }

    public void pushConstants(Pipeline pipeline) {
        VkCommandBuffer commandBuffer = this.currentCmdBuffer;
        PushConstants pushConstants = pipeline.getPushConstants();
        try (MemoryStack stack = MemoryStack.stackPush();){
            ByteBuffer buffer = stack.malloc(pushConstants.getSize());
            long ptr = MemoryUtil.memAddress0((Buffer)buffer);
            pushConstants.update(ptr);
            VK10.nvkCmdPushConstants((VkCommandBuffer)commandBuffer, (long)pipeline.getLayout(), (int)1, (int)0, (int)pushConstants.getSize(), (long)ptr);
        }
    }

    public Pipeline getBoundPipeline() {
        return this.boundPipeline;
    }

    public void setBoundFramebuffer(Framebuffer framebuffer) {
        this.boundFramebuffer = framebuffer;
    }

    public Framebuffer getBoundFramebuffer() {
        return this.boundFramebuffer;
    }

    public void setBoundRenderPass(RenderPass boundRenderPass) {
        this.boundRenderPass = boundRenderPass;
    }

    public RenderPass getBoundRenderPass() {
        return this.boundRenderPass;
    }

    public void setMainPass(MainPass mainPass) {
        this.mainPass = mainPass;
    }

    public MainPass getMainPass() {
        return this.mainPass;
    }

    public SwapChain getSwapChain() {
        return this.swapChain;
    }

    public CommandPool.CommandBuffer getTransferCb() {
        return this.transferCbs.get(currentFrame);
    }

    private static void resetDynamicState(VkCommandBuffer commandBuffer) {
        VK10.vkCmdSetDepthBias((VkCommandBuffer)commandBuffer, (float)0.0f, (float)0.0f, (float)0.0f);
        VK10.vkCmdSetLineWidth((VkCommandBuffer)commandBuffer, (float)1.0f);
    }

    public static void setDepthBias(float constant, float slope) {
        VkCommandBuffer commandBuffer = Renderer.INSTANCE.currentCmdBuffer;
        VK10.vkCmdSetDepthBias((VkCommandBuffer)commandBuffer, (float)constant, (float)0.0f, (float)slope);
    }

    public static void clearAttachments(int attachments) {
        Renderer.clearAttachments(Renderer.INSTANCE.currentCmdBuffer, attachments);
    }

    public static void clearAttachments(VkCommandBuffer commandBuffer, int attachments) {
        Framebuffer framebuffer = Renderer.getInstance().boundFramebuffer;
        if (framebuffer == null) {
            return;
        }
        Renderer.clearAttachments(commandBuffer, attachments, framebuffer.getWidth(), framebuffer.getHeight());
    }

    public static void clearAttachments(int attachments, int width, int height) {
        Renderer.clearAttachments(Renderer.INSTANCE.currentCmdBuffer, attachments, width, height);
    }

    public static void clearAttachments(int attachments, int x, int y, int width, int height) {
        Renderer.clearAttachments(Renderer.INSTANCE.currentCmdBuffer, attachments, x, y, width, height);
    }

    public static void clearAttachments(VkCommandBuffer commandBuffer, int attachments, int width, int height) {
        Renderer.clearAttachments(commandBuffer, attachments, 0, 0, width, height);
    }

    public static void clearAttachments(VkCommandBuffer commandBuffer, int attachments, int x, int y, int width, int height) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkClearValue colorValue = VkClearValue.calloc((MemoryStack)stack);
            colorValue.color().float32(VRenderSystem.clearColor);
            VkClearValue depthValue = VkClearValue.calloc((MemoryStack)stack);
            depthValue.depthStencil().set(VRenderSystem.clearDepthValue, 0);
            int attachmentsCount = attachments == 16640 ? 2 : 1;
            VkClearAttachment.Buffer pAttachments = VkClearAttachment.malloc((int)attachmentsCount, (MemoryStack)stack);
            switch (attachments) {
                case 256: {
                    VkClearAttachment clearDepth = (VkClearAttachment)pAttachments.get(0);
                    clearDepth.aspectMask(2);
                    clearDepth.colorAttachment(0);
                    clearDepth.clearValue(depthValue);
                    break;
                }
                case 16384: {
                    VkClearAttachment clearColor = (VkClearAttachment)pAttachments.get(0);
                    clearColor.aspectMask(1);
                    clearColor.colorAttachment(0);
                    clearColor.clearValue(colorValue);
                    break;
                }
                case 16640: {
                    VkClearAttachment clearColor = (VkClearAttachment)pAttachments.get(0);
                    clearColor.aspectMask(1);
                    clearColor.colorAttachment(0);
                    clearColor.clearValue(colorValue);
                    VkClearAttachment clearDepth = (VkClearAttachment)pAttachments.get(1);
                    clearDepth.aspectMask(2);
                    clearDepth.colorAttachment(0);
                    clearDepth.clearValue(depthValue);
                    break;
                }
                default: {
                    throw new RuntimeException("unexpected value");
                }
            }
            VkRect2D renderArea = VkRect2D.malloc((MemoryStack)stack);
            renderArea.offset().set(x, y);
            renderArea.extent().set(width, height);
            VkClearRect.Buffer pRect = VkClearRect.malloc((int)1, (MemoryStack)stack);
            pRect.rect(renderArea);
            pRect.baseArrayLayer(0);
            pRect.layerCount(1);
            VK10.vkCmdClearAttachments((VkCommandBuffer)commandBuffer, (VkClearAttachment.Buffer)pAttachments, (VkClearRect.Buffer)pRect);
        }
    }

    public static void setInvertedViewport(int x, int y, int width, int height) {
        Renderer.setViewportState(x, y + height, width, -height);
    }

    public static void resetViewport() {
        int width = INSTANCE.getSwapChain().getWidth();
        int height = INSTANCE.getSwapChain().getHeight();
        Renderer.setViewportState(0, 0, width, height);
    }

    public static void setViewportState(int x, int y, int width, int height) {
        GlStateManager._viewport((int)x, (int)y, (int)width, (int)height);
    }

    public static void setViewport(int x, int y, int width, int height) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            Renderer.setViewport(x, y, width, height, stack);
        }
    }

    public static void setViewport(int x, int y, int width, int height, MemoryStack stack) {
        if (!Renderer.INSTANCE.recordingCmds) {
            return;
        }
        VkViewport.Buffer viewport = VkViewport.malloc((int)1, (MemoryStack)stack);
        viewport.x((float)x);
        viewport.y((float)(height + y));
        viewport.width((float)width);
        viewport.height((float)(-height));
        viewport.minDepth(0.0f);
        viewport.maxDepth(1.0f);
        VK10.vkCmdSetViewport((VkCommandBuffer)Renderer.INSTANCE.currentCmdBuffer, (int)0, (VkViewport.Buffer)viewport);
    }

    public static void setScissor(int x, int y, int width, int height) {
        if (!Renderer.INSTANCE.recordingCmds || Renderer.INSTANCE.boundFramebuffer == null) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush();){
            int framebufferHeight = Renderer.INSTANCE.boundFramebuffer.getHeight();
            x = Math.max(0, x);
            VkRect2D.Buffer scissor = VkRect2D.malloc((int)1, (MemoryStack)stack);
            scissor.offset().set(x, framebufferHeight - (y + height));
            scissor.extent().set(width, height);
            VK10.vkCmdSetScissor((VkCommandBuffer)Renderer.INSTANCE.currentCmdBuffer, (int)0, (VkRect2D.Buffer)scissor);
        }
    }

    public static void resetScissor() {
        if (Renderer.INSTANCE.boundFramebuffer == null) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkRect2D.Buffer scissor = Renderer.INSTANCE.boundFramebuffer.scissor(stack);
            VK10.vkCmdSetScissor((VkCommandBuffer)Renderer.INSTANCE.currentCmdBuffer, (int)0, (VkRect2D.Buffer)scissor);
        }
    }

    public static void pushDebugSection(String s) {
    }

    public static void popDebugSection() {
    }

    public static void popPushDebugSection(String s) {
        Renderer.popDebugSection();
        Renderer.pushDebugSection(s);
    }

    public static int getFramesNum() {
        return Renderer.INSTANCE.framesNum;
    }

    public static VkCommandBuffer getCommandBuffer() {
        return Renderer.INSTANCE.currentCmdBuffer;
    }

    public static boolean isRecording() {
        return Renderer.INSTANCE.recordingCmds;
    }

    public static void scheduleSwapChainUpdate() {
        swapChainUpdate = true;
    }

    static {
        swapChainUpdate = false;
        currentFrame = 0;
        lastReset = -1;
    }
}

