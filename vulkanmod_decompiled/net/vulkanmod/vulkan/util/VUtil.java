/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 */
package net.vulkanmod.vulkan.util;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Collection;
import net.vulkanmod.vulkan.memory.buffer.Buffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import sun.misc.Unsafe;

public class VUtil {
    public static final boolean CHECKS = true;
    public static final int UINT32_MAX = -1;
    public static final long UINT64_MAX = -1L;
    public static final Unsafe UNSAFE;

    public static PointerBuffer asPointerBuffer(Collection<String> collection) {
        MemoryStack stack = MemoryStack.stackGet();
        PointerBuffer buffer = stack.mallocPointer(collection.size());
        collection.stream().map(arg_0 -> ((MemoryStack)stack).UTF8(arg_0)).forEach(arg_0 -> ((PointerBuffer)buffer).put(arg_0));
        return (PointerBuffer)buffer.rewind();
    }

    public static void memcpy(ByteBuffer src, long dstPtr) {
        MemoryUtil.memCopy((long)MemoryUtil.memAddress0((java.nio.Buffer)src), (long)dstPtr, (long)src.capacity());
    }

    public static void memcpy(ByteBuffer src, Buffer dst, long size) {
        if (size > dst.getBufferSize() - dst.getUsedBytes()) {
            throw new IllegalArgumentException("Upload size is greater than available dst buffer size");
        }
        long srcPtr = MemoryUtil.memAddress((ByteBuffer)src);
        long dstPtr = dst.getDataPtr() + dst.getUsedBytes();
        MemoryUtil.memCopy((long)srcPtr, (long)dstPtr, (long)size);
    }

    public static void memcpy(Buffer src, ByteBuffer dst, long size) {
        if (size > (long)dst.remaining()) {
            throw new IllegalArgumentException("Upload size is greater than available dst buffer size");
        }
        long srcPtr = src.getDataPtr();
        long dstPtr = MemoryUtil.memAddress((ByteBuffer)dst);
        MemoryUtil.memCopy((long)srcPtr, (long)dstPtr, (long)size);
    }

    public static void memcpy(ByteBuffer src, Buffer dst, long size, long srcOffset, long dstOffset) {
        if (size > dst.getBufferSize() - dstOffset) {
            throw new IllegalArgumentException("Upload size is greater than available dst buffer size");
        }
        long dstPtr = dst.getDataPtr() + dstOffset;
        long srcPtr = MemoryUtil.memAddress((ByteBuffer)src) + srcOffset;
        MemoryUtil.memCopy((long)srcPtr, (long)dstPtr, (long)size);
    }

    public static int align(int x, int align) {
        int r = x % align;
        return r == 0 ? x : x + align - r;
    }

    static {
        Field f = null;
        try {
            f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe)f.get(null);
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}

