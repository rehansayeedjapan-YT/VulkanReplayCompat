/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.apache.commons.lang3.Validate
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.util.vma.Vma
 *  org.lwjgl.util.vma.VmaAllocationCreateInfo
 *  org.lwjgl.util.vma.VmaBudget
 *  org.lwjgl.util.vma.VmaBudget$Buffer
 *  org.lwjgl.vulkan.VkBufferCreateInfo
 *  org.lwjgl.vulkan.VkImageCreateInfo
 */
package net.vulkanmod.vulkan.memory;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.LongBuffer;
import java.util.function.Consumer;
import net.vulkanmod.Initializer;
import net.vulkanmod.render.chunk.buffer.AreaBuffer;
import net.vulkanmod.vulkan.Vulkan;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.memory.MemoryType;
import net.vulkanmod.vulkan.memory.MemoryTypes;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import net.vulkanmod.vulkan.queue.Queue;
import net.vulkanmod.vulkan.texture.VulkanImage;
import net.vulkanmod.vulkan.util.Pair;
import net.vulkanmod.vulkan.util.VkResult;
import org.apache.commons.lang3.Validate;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaBudget;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkImageCreateInfo;

public class MemoryManager {
    private static final boolean DEBUG = false;
    public static final long BYTES_IN_MB = 0x100000L;
    private static MemoryManager INSTANCE;
    private static final long ALLOCATOR;
    private static final Long2ReferenceOpenHashMap<Buffer> buffers;
    private static final Long2ReferenceOpenHashMap<VulkanImage> images;
    static int Frames;
    private static long deviceMemory;
    private static long nativeMemory;
    private int currentFrame = 0;
    private final ObjectArrayList<Buffer.BufferInfo>[] freeableBuffers = new ObjectArrayList[Frames];
    private final ObjectArrayList<VulkanImage>[] freeableImages = new ObjectArrayList[Frames];
    private final ObjectArrayList<Runnable>[] frameOps = new ObjectArrayList[Frames];
    private final ObjectArrayList<Pair<AreaBuffer, Integer>>[] segmentsToFree = new ObjectArrayList[Frames];
    private ObjectArrayList<StackTraceElement[]>[] stackTraces;

    public static MemoryManager getInstance() {
        return INSTANCE;
    }

    public static void createInstance(int frames) {
        Frames = frames;
        INSTANCE = new MemoryManager();
    }

    MemoryManager() {
        for (int i = 0; i < Frames; ++i) {
            this.freeableBuffers[i] = new ObjectArrayList();
            this.freeableImages[i] = new ObjectArrayList();
            this.frameOps[i] = new ObjectArrayList();
            this.segmentsToFree[i] = new ObjectArrayList();
        }
    }

    public synchronized void initFrame(int frame) {
        this.setCurrentFrame(frame);
        this.freeBuffers(frame);
        this.freeImages(frame);
        this.doFrameOps(frame);
        this.freeSegments(frame);
    }

    public void setCurrentFrame(int frame) {
        Validate.isTrue((frame < Frames ? 1 : 0) != 0, (String)"Out of bounds frame index", (Object[])new Object[0]);
        this.currentFrame = frame;
    }

    public void freeAllBuffers() {
        for (int frame = 0; frame < Frames; ++frame) {
            this.freeBuffers(frame);
            this.freeImages(frame);
            this.doFrameOps(frame);
        }
    }

    public void createBuffer(long size, int usage, int properties, LongBuffer pBuffer, PointerBuffer pBufferMemory) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc((MemoryStack)stack);
            bufferInfo.sType(12);
            bufferInfo.size(size);
            bufferInfo.usage(usage);
            VmaAllocationCreateInfo allocationInfo = VmaAllocationCreateInfo.calloc((MemoryStack)stack);
            allocationInfo.requiredFlags(properties);
            int result = Vma.vmaCreateBuffer((long)ALLOCATOR, (VkBufferCreateInfo)bufferInfo, (VmaAllocationCreateInfo)allocationInfo, (LongBuffer)pBuffer, (PointerBuffer)pBufferMemory, null);
            if (result != 0) {
                Initializer.LOGGER.info(String.format("Failed to create buffer with size: %.3f MB", Float.valueOf((float)size / 1048576.0f)));
                Initializer.LOGGER.info(String.format("Tracked Device Memory used: %d/%d MB", this.getAllocatedDeviceMemoryMB(), this.getDeviceMemoryMB()));
                Initializer.LOGGER.info(this.getHeapStats());
                throw new RuntimeException("Failed to create buffer: %s".formatted(VkResult.decode(result)));
            }
        }
    }

    public synchronized void createBuffer(Buffer buffer, long size, int usage, int properties) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            LongBuffer pBuffer = stack.mallocLong(1);
            PointerBuffer pAllocation = stack.pointers(0L);
            this.createBuffer(size, usage, properties, pBuffer, pAllocation);
            buffer.setId(pBuffer.get(0));
            buffer.setAllocation(pAllocation.get(0));
            buffer.setBufferSize(size);
            if ((properties & 1) != 0) {
                deviceMemory += size;
            } else {
                nativeMemory += size;
            }
            buffers.putIfAbsent(buffer.getId(), (Object)buffer);
        }
    }

    public void createImage(int width, int height, int arrayLayers, int mipLevels, int format, int tiling, int usage, int flags, int memProperties, LongBuffer pTextureImage, PointerBuffer pTextureImageMemory) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc((MemoryStack)stack);
            imageInfo.sType(14);
            imageInfo.imageType(1);
            imageInfo.extent().width(width);
            imageInfo.extent().height(height);
            imageInfo.extent().depth(1);
            imageInfo.mipLevels(mipLevels);
            imageInfo.arrayLayers(arrayLayers);
            imageInfo.format(format);
            imageInfo.tiling(tiling);
            imageInfo.initialLayout(0);
            imageInfo.usage(usage);
            imageInfo.samples(1);
            imageInfo.flags(flags);
            imageInfo.pQueueFamilyIndices(stack.ints(Queue.getQueueFamilies().graphicsFamily, Queue.getQueueFamilies().computeFamily));
            VmaAllocationCreateInfo allocationInfo = VmaAllocationCreateInfo.calloc((MemoryStack)stack);
            allocationInfo.requiredFlags(memProperties);
            int result = Vma.vmaCreateImage((long)ALLOCATOR, (VkImageCreateInfo)imageInfo, (VmaAllocationCreateInfo)allocationInfo, (LongBuffer)pTextureImage, (PointerBuffer)pTextureImageMemory, null);
            if (result != 0) {
                Initializer.LOGGER.info(String.format("Failed to create image with size: %dx%d", width, height));
                throw new RuntimeException("Failed to create image: %s".formatted(VkResult.decode(result)));
            }
        }
    }

    public static void addImage(VulkanImage image) {
        images.putIfAbsent(image.getId(), (Object)image);
        deviceMemory += (long)image.size;
    }

    public static void MapAndCopy(long allocation, Consumer<PointerBuffer> consumer) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            PointerBuffer data = stack.mallocPointer(1);
            Vma.vmaMapMemory((long)ALLOCATOR, (long)allocation, (PointerBuffer)data);
            consumer.accept(data);
            Vma.vmaUnmapMemory((long)ALLOCATOR, (long)allocation);
        }
    }

    public PointerBuffer Map(long allocation) {
        PointerBuffer data = MemoryUtil.memAllocPointer((int)1);
        Vma.vmaMapMemory((long)ALLOCATOR, (long)allocation, (PointerBuffer)data);
        return data;
    }

    public static void freeBuffer(long buffer, long allocation) {
        Vma.vmaDestroyBuffer((long)ALLOCATOR, (long)buffer, (long)allocation);
        buffers.remove(buffer);
    }

    private static void freeBuffer(Buffer.BufferInfo bufferInfo) {
        Vma.vmaDestroyBuffer((long)ALLOCATOR, (long)bufferInfo.id(), (long)bufferInfo.allocation());
        if (bufferInfo.type() == MemoryType.Type.DEVICE_LOCAL) {
            deviceMemory -= bufferInfo.bufferSize();
        } else {
            nativeMemory -= bufferInfo.bufferSize();
        }
        buffers.remove(bufferInfo.id());
    }

    public static void freeImage(long imageId, long allocation) {
        Vma.vmaDestroyImage((long)ALLOCATOR, (long)imageId, (long)allocation);
        VulkanImage image = (VulkanImage)images.remove(imageId);
        deviceMemory -= (long)image.size;
    }

    public synchronized void addToFreeable(Buffer buffer) {
        Buffer.BufferInfo bufferInfo = buffer.getBufferInfo();
        this.checkBuffer(bufferInfo);
        this.freeableBuffers[this.currentFrame].add((Object)bufferInfo);
    }

    public synchronized void addToFreeable(VulkanImage image) {
        this.freeableImages[this.currentFrame].add((Object)image);
    }

    public synchronized void addFrameOp(Runnable runnable) {
        this.frameOps[this.currentFrame].add((Object)runnable);
    }

    public void doFrameOps(int frame) {
        for (Runnable runnable : this.frameOps[frame]) {
            runnable.run();
        }
        this.frameOps[frame].clear();
    }

    private void freeBuffers(int frame) {
        ObjectArrayList<Buffer.BufferInfo> bufferList = this.freeableBuffers[frame];
        for (Buffer.BufferInfo bufferInfo : bufferList) {
            MemoryManager.freeBuffer(bufferInfo);
        }
        bufferList.clear();
    }

    private void freeImages(int frame) {
        ObjectArrayList<VulkanImage> bufferList = this.freeableImages[frame];
        for (VulkanImage image : bufferList) {
            image.doFree();
        }
        bufferList.clear();
    }

    private void checkBuffer(Buffer.BufferInfo bufferInfo) {
        if (buffers.get(bufferInfo.id()) == null) {
            throw new RuntimeException("trying to free not present buffer");
        }
    }

    private void freeSegments(int frame) {
        ObjectArrayList<Pair<AreaBuffer, Integer>> list = this.segmentsToFree[frame];
        for (Pair pair : list) {
            ((AreaBuffer)pair.first).setSegmentFree((Integer)pair.second);
        }
        list.clear();
    }

    public void addToFreeSegment(AreaBuffer areaBuffer, int offset) {
        this.segmentsToFree[this.currentFrame].add(new Pair<AreaBuffer, Integer>(areaBuffer, offset));
    }

    public int getNativeMemoryMB() {
        return this.bytesInMb(nativeMemory);
    }

    public int getAllocatedDeviceMemoryMB() {
        return this.bytesInMb(deviceMemory);
    }

    public int getDeviceMemoryMB() {
        return this.bytesInMb(MemoryTypes.GPU_MEM.vkMemoryHeap.size());
    }

    int bytesInMb(long bytes) {
        return (int)(bytes / 0x100000L);
    }

    public String getHeapStats() {
        try (MemoryStack stack = MemoryStack.stackPush();){
            VmaBudget.Buffer vmaBudgets = VmaBudget.calloc((int)DeviceManager.memoryProperties.memoryHeapCount(), (MemoryStack)stack);
            Vma.vmaGetHeapBudgets((long)ALLOCATOR, (VmaBudget.Buffer)vmaBudgets);
            VmaBudget vmaBudget = (VmaBudget)vmaBudgets.get(MemoryTypes.GPU_MEM.vkMemoryType.heapIndex());
            long usage = vmaBudget.usage();
            long budget = vmaBudget.budget();
            String string = String.format("Device Memory Heap Usage: %d/%dMB", this.bytesInMb(usage), this.bytesInMb(budget));
            return string;
        }
    }

    static {
        ALLOCATOR = Vulkan.getAllocator();
        buffers = new Long2ReferenceOpenHashMap();
        images = new Long2ReferenceOpenHashMap();
        deviceMemory = 0L;
        nativeMemory = 0L;
    }
}

