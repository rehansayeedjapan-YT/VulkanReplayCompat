/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBuffer$MappedView
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.vulkanmod.render.engine;

import com.mojang.blaze3d.buffers.GpuBuffer;
import java.nio.ByteBuffer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.vulkanmod.render.engine.VkDebugLabel;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.memory.MemoryType;
import net.vulkanmod.vulkan.memory.MemoryTypes;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class VkGpuBuffer
extends GpuBuffer {
    protected boolean closed;
    @Nullable
    protected final Supplier<String> label;
    Buffer buffer;

    protected VkGpuBuffer(VkDebugLabel debugLabel, @Nullable Supplier<String> supplier, int usage, long size) {
        super(usage, size);
        this.label = supplier;
        int vkUsage = 0;
        if ((usage & 0x10) != 0) {
            vkUsage |= 1;
        }
        if ((usage & 8) != 0) {
            vkUsage |= 2;
        }
        if ((usage & 0x20) != 0) {
            vkUsage |= 0x80;
        }
        if ((usage & 0x40) != 0) {
            vkUsage |= 0x40;
        }
        if ((usage & 0x80) != 0) {
            vkUsage |= 0x10;
        }
        if ((usage & 0x100) != 0) {
            vkUsage |= 4;
        }
        boolean mappable = (usage & 1) != 0 | (usage & 2) != 0 | (usage & 4) != 0;
        MemoryType memoryType = mappable ? MemoryTypes.HOST_MEM : MemoryTypes.GPU_MEM;
        this.buffer = new Buffer(supplier.get(), vkUsage, memoryType);
        this.buffer.createBuffer(this.size());
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void close() {
        if (!this.closed) {
            this.closed = true;
            MemoryManager.getInstance().addToFreeable(this.buffer);
        }
    }

    public Buffer getBuffer() {
        return this.buffer;
    }

    public static int bufferUsageToGlEnum(int i) {
        boolean stream;
        boolean bl = stream = (i & 4) != 0;
        if ((i & 2) != 0) {
            return stream ? 35040 : 35044;
        }
        if ((i & 1) != 0) {
            return stream ? 35041 : 35045;
        }
        return 35044;
    }

    @Environment(value=EnvType.CLIENT)
    public static class MappedView
    implements GpuBuffer.MappedView {
        private final int target;
        private final ByteBuffer data;

        protected MappedView(int i, ByteBuffer byteBuffer) {
            this.target = i;
            this.data = byteBuffer;
        }

        public ByteBuffer data() {
            return this.data;
        }

        public void close() {
        }
    }
}

