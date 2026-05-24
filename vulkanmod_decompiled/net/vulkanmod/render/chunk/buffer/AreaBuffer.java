/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
 *  org.apache.logging.log4j.Logger
 */
package net.vulkanmod.render.chunk.buffer;

import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import java.nio.ByteBuffer;
import net.vulkanmod.Initializer;
import net.vulkanmod.render.chunk.buffer.DrawParametersBuffer;
import net.vulkanmod.render.chunk.buffer.UploadManager;
import net.vulkanmod.render.chunk.util.Util;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.memory.MemoryType;
import net.vulkanmod.vulkan.memory.MemoryTypes;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import net.vulkanmod.vulkan.memory.buffer.IndexBuffer;
import net.vulkanmod.vulkan.memory.buffer.VertexBuffer;
import org.apache.logging.log4j.Logger;

public class AreaBuffer {
    private static final boolean DEBUG = false;
    private static final Logger LOGGER = Initializer.LOGGER;
    private static final MemoryType MEMORY_TYPE = MemoryTypes.GPU_MEM;
    private final int usage;
    private final int elementSize;
    private final Int2ReferenceOpenHashMap<Segment> usedSegments = new Int2ReferenceOpenHashMap();
    Segment first;
    Segment last;
    private Buffer buffer;
    int size;
    int used = 0;
    int segments = 0;

    public AreaBuffer(Usage usage, int elementCount, int elementSize) {
        this.usage = usage.usage;
        this.elementSize = elementSize;
        this.size = elementCount * elementSize;
        this.buffer = this.allocateBuffer();
        Segment s = new Segment(0, this.size);
        ++this.segments;
        this.last = this.first = s;
    }

    private Buffer allocateBuffer() {
        Buffer buffer = this.usage == Usage.VERTEX.usage ? new VertexBuffer(this.size, MEMORY_TYPE) : new IndexBuffer(this.size, MEMORY_TYPE);
        return buffer;
    }

    public Segment allocateSegment(int size) {
        Segment segment = this.findSegment(size);
        if (segment.size - size > 0) {
            Segment s1 = new Segment(segment.offset + size, segment.size - size);
            ++this.segments;
            if (segment.next != null) {
                s1.bindNext(segment.next);
            } else {
                this.last = s1;
            }
            segment.bindNext(s1);
            segment.size = size;
        }
        segment.free = false;
        this.usedSegments.put(segment.offset, (Object)segment);
        segment.paramsPtr = 0L;
        this.used += size;
        return segment;
    }

    public void freeSegment(int offset) {
        if (offset != -1) {
            MemoryManager.getInstance().addToFreeSegment(this, offset);
        }
    }

    public void upload(Segment segment, ByteBuffer byteBuffer, int offset) {
        int size = byteBuffer.remaining();
        if (size + offset > segment.size) {
            throw new RuntimeException("trying to upload %d at offset %d, but segment size is %d".formatted(size, offset, segment.size));
        }
        Buffer dst = this.buffer;
        UploadManager.INSTANCE.recordUpload(dst, segment.offset + offset, size, byteBuffer);
    }

    public Segment upload(ByteBuffer byteBuffer, int oldOffset, long paramsPtr) {
        this.freeSegment(oldOffset);
        int size = byteBuffer.remaining();
        Segment segment = this.findSegment(size);
        if (segment.size - size > 0) {
            Segment s1 = new Segment(segment.offset + size, segment.size - size);
            ++this.segments;
            if (segment.next != null) {
                s1.bindNext(segment.next);
            } else {
                this.last = s1;
            }
            segment.bindNext(s1);
            segment.size = size;
        }
        segment.free = false;
        this.usedSegments.put(segment.offset, (Object)segment);
        segment.paramsPtr = paramsPtr;
        Buffer dst = this.buffer;
        UploadManager.INSTANCE.recordUpload(dst, segment.offset, size, byteBuffer);
        this.used += size;
        return segment;
    }

    public Segment findSegment(int size) {
        Segment segment = null;
        Segment segment1 = this.first;
        while (segment1 != null) {
            if (segment1.isFree() && segment1.size >= size && (segment == null || segment1.size < segment.size)) {
                segment = segment1;
            }
            segment1 = segment1.next;
        }
        if (segment == null || segment.size < size) {
            return this.reallocate(size);
        }
        return segment;
    }

    public Segment reallocate(int uploadSize) {
        int newSize;
        int oldSize = this.size;
        int minIncrement = this.size >> 3;
        int increment = Math.max(minIncrement = (int)Util.align(minIncrement, this.elementSize), uploadSize);
        if (increment < uploadSize) {
            throw new RuntimeException(String.format("Size increment %d < %d (Upload size)", increment, uploadSize));
        }
        this.size = newSize = oldSize + increment;
        Buffer dst = this.allocateBuffer();
        UploadManager.INSTANCE.copyBuffer(this.buffer, dst);
        this.buffer.scheduleFree();
        this.buffer = dst;
        if (this.last.isFree()) {
            this.last.size += increment;
        } else {
            int offset = this.last.offset + this.last.size;
            Segment segment = new Segment(offset, newSize - offset);
            ++this.segments;
            this.last.bindNext(segment);
            this.last = segment;
        }
        return this.last;
    }

    void moveUsedSegments(Buffer dst) {
        int dstOffset;
        int usedCount = 0;
        int currOffset = dstOffset = 0;
        Segment segment = this.first;
        Segment prevUsed = null;
        int srcOffset = -1;
        int uploadSize = 0;
        while (segment != null) {
            if (!segment.isFree()) {
                ++usedCount;
                if (segment.offset != srcOffset + uploadSize) {
                    if (srcOffset == -1) {
                        dstOffset = 0;
                        this.first = segment;
                        segment.prev = null;
                    } else {
                        UploadManager.INSTANCE.copyBuffer(this.buffer, srcOffset, dst, dstOffset, uploadSize);
                        dstOffset += uploadSize;
                    }
                    srcOffset = segment.offset;
                    uploadSize = segment.size;
                } else {
                    uploadSize += segment.size;
                }
                this.usedSegments.remove(segment.offset);
                segment.offset = currOffset;
                currOffset += segment.size;
                this.updateDrawParams(segment);
                this.usedSegments.put(segment.offset, (Object)segment);
                if (prevUsed != null) {
                    prevUsed.bindNext(segment);
                }
                prevUsed = segment;
            }
            segment = segment.next;
        }
        if (uploadSize > 0) {
            UploadManager.INSTANCE.copyBuffer(this.buffer, srcOffset, dst, dstOffset, uploadSize);
        }
        if (prevUsed != null) {
            prevUsed.next = null;
            this.last = prevUsed;
            this.segments = usedCount;
        }
    }

    public void setSegmentFree(int offset) {
        Segment prev;
        Segment segment = (Segment)this.usedSegments.remove(offset * this.elementSize);
        if (segment == null) {
            return;
        }
        this.used -= segment.size;
        segment.free = true;
        segment.paramsPtr = -1L;
        Segment next = segment.next;
        if (next != null && next.isFree()) {
            this.mergeSegments(segment, next);
        }
        if ((prev = segment.prev) != null && prev.isFree()) {
            this.mergeSegments(prev, segment);
        }
    }

    private void mergeSegments(Segment segment, Segment next) {
        segment.size += next.size;
        if (next.next != null) {
            next.next.prev = segment;
        } else {
            this.last = segment;
        }
        segment.next = next.next;
        --this.segments;
    }

    private void updateDrawParams(Segment segment) {
        int elementOffset = segment.offset / this.elementSize;
        if (this.usage == Usage.VERTEX.usage) {
            DrawParametersBuffer.setVertexOffset(segment.paramsPtr, elementOffset);
        } else {
            DrawParametersBuffer.setFirstIndex(segment.paramsPtr, elementOffset);
        }
    }

    public long getId() {
        return this.buffer.getId();
    }

    public void freeBuffer() {
        this.buffer.scheduleFree();
    }

    public int fragmentation() {
        return this.size - this.used - (this.last.isFree() ? this.last.size : 0);
    }

    public void checkSegments() {
        Segment segment = this.first;
        Segment prev = null;
        int i = 0;
        int usedSegments = 0;
        if (segment.offset != 0) {
            LOGGER.error(String.format("expected first offset 0 but got %d", segment.offset));
        }
        while (segment != null) {
            Segment next;
            if (i >= this.segments) {
                LOGGER.error("Count is greater than segments");
                break;
            }
            if (segment.prev != prev) {
                LOGGER.error(String.format("expected previous segment not matching (segment %d)", i));
            }
            if (!segment.isFree()) {
                ++usedSegments;
            }
            if (segment.offset % this.elementSize != 0) {
                LOGGER.error(String.format("offset %d misaligned (segment %d)", segment.offset, i));
            }
            if ((next = segment.next) != null) {
                int offset = segment.offset + segment.size;
                if (offset != next.offset) {
                    LOGGER.error(String.format("expected offset %d but got %d (segment %d)", offset, next.offset, i));
                }
                if (next.prev != segment) {
                    LOGGER.error(String.format("segment pointer not correct (segment %d)", i));
                }
            } else if (segment != this.last) {
                LOGGER.error(String.format("segment has no next pointer and it's not last (segment %d)", i));
            } else {
                int segmentEnd = segment.offset + segment.size;
                if (segment.offset + segment.size != this.size) {
                    LOGGER.error(String.format("last segment end (%d) does not match buffer size (%d)", segmentEnd, this.size));
                }
                if (segment.offset != this.used) {
                    LOGGER.error(String.format("last segment offset (%d) does not match buffer used size (%d)", segmentEnd, this.size));
                }
            }
            prev = segment;
            segment = next;
            ++i;
        }
        if (i != this.segments) {
            LOGGER.error("Count do not match segments");
        }
        if (usedSegments != this.usedSegments.size()) {
            LOGGER.error("Counted used segment do not match used segments map size");
        }
    }

    public int getSize() {
        return this.size;
    }

    public int getUsed() {
        return this.used;
    }

    public static enum Usage {
        VERTEX(0),
        INDEX(1);

        final int usage;

        private Usage(int i) {
            this.usage = i;
        }
    }

    public static class Segment {
        int offset;
        int size;
        boolean free = true;
        long paramsPtr;
        Segment next;
        Segment prev;

        private Segment(int offset, int size) {
            this.offset = offset;
            this.size = size;
        }

        public int getOffset() {
            return this.offset;
        }

        public int getSize() {
            return this.size;
        }

        public boolean isFree() {
            return this.free;
        }

        public void setFree(boolean free) {
            this.free = free;
        }

        public void bindNext(Segment s) {
            this.next = s;
            s.prev = this;
        }
    }
}

