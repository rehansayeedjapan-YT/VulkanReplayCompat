/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.system.MemoryUtil
 */
package net.vulkanmod.render.vertex;

import org.lwjgl.system.MemoryUtil;

public interface VertexBuilder {
    public void vertex(long var1, float var3, float var4, float var5, int var6, float var7, float var8, int var9, int var10);

    public void position(long var1, float var3, float var4, float var5);

    public void color(long var1, int var3);

    public void uv(long var1, float var3, float var4);

    public void light(long var1, int var3);

    public void normal(long var1, int var3);

    public int getStride();

    public static class CompressedVertexBuilder
    implements VertexBuilder {
        private static final int VERTEX_SIZE = 16;
        public static final float POS_CONV_MUL = 2048.0f;
        public static final float POS_OFFSET = -4.0f;
        public static final float POS_OFFSET_CONV = -8192.0f;
        public static final float UV_CONV_MUL = 32768.0f;

        @Override
        public void vertex(long ptr, float x, float y, float z, int color, float u, float v, int light, int packedNormal) {
            short sX = (short)(x * 2048.0f + -8192.0f);
            short sY = (short)(y * 2048.0f + -8192.0f);
            short sZ = (short)(z * 2048.0f + -8192.0f);
            MemoryUtil.memPutShort((long)(ptr + 0L), (short)sX);
            MemoryUtil.memPutShort((long)(ptr + 2L), (short)sY);
            MemoryUtil.memPutShort((long)(ptr + 4L), (short)sZ);
            short l = (short)(light >>> 8 & 0xFF00 | light & 0xFF);
            MemoryUtil.memPutShort((long)(ptr + 6L), (short)l);
            MemoryUtil.memPutShort((long)(ptr + 8L), (short)((short)(u * 32768.0f)));
            MemoryUtil.memPutShort((long)(ptr + 10L), (short)((short)(v * 32768.0f)));
            MemoryUtil.memPutInt((long)(ptr + 12L), (int)color);
        }

        @Override
        public void position(long ptr, float x, float y, float z) {
            short sX = (short)(x * 2048.0f + -8192.0f);
            short sY = (short)(y * 2048.0f + -8192.0f);
            short sZ = (short)(z * 2048.0f + -8192.0f);
            MemoryUtil.memPutShort((long)(ptr + 0L), (short)sX);
            MemoryUtil.memPutShort((long)(ptr + 2L), (short)sY);
            MemoryUtil.memPutShort((long)(ptr + 4L), (short)sZ);
        }

        @Override
        public void color(long ptr, int color) {
            MemoryUtil.memPutInt((long)(ptr + 12L), (int)color);
        }

        @Override
        public void uv(long ptr, float u, float v) {
            MemoryUtil.memPutShort((long)(ptr + 8L), (short)((short)(u * 32768.0f)));
            MemoryUtil.memPutShort((long)(ptr + 10L), (short)((short)(v * 32768.0f)));
        }

        @Override
        public void light(long ptr, int light) {
            short l = (short)(light >>> 8 & 0xFF00 | light & 0xFF);
            MemoryUtil.memPutShort((long)(ptr + 6L), (short)l);
        }

        @Override
        public void normal(long ptr, int normal) {
        }

        @Override
        public int getStride() {
            return 16;
        }
    }

    public static class DefaultVertexBuilder
    implements VertexBuilder {
        private static final int VERTEX_SIZE = 32;

        @Override
        public void vertex(long ptr, float x, float y, float z, int color, float u, float v, int light, int packedNormal) {
            MemoryUtil.memPutFloat((long)(ptr + 0L), (float)x);
            MemoryUtil.memPutFloat((long)(ptr + 4L), (float)y);
            MemoryUtil.memPutFloat((long)(ptr + 8L), (float)z);
            MemoryUtil.memPutInt((long)(ptr + 12L), (int)color);
            MemoryUtil.memPutFloat((long)(ptr + 16L), (float)u);
            MemoryUtil.memPutFloat((long)(ptr + 20L), (float)v);
            MemoryUtil.memPutShort((long)(ptr + 24L), (short)((short)(light & 0xFFFF)));
            MemoryUtil.memPutShort((long)(ptr + 26L), (short)((short)(light >> 16 & 0xFFFF)));
            MemoryUtil.memPutInt((long)(ptr + 28L), (int)packedNormal);
        }

        @Override
        public void position(long ptr, float x, float y, float z) {
        }

        @Override
        public void color(long ptr, int color) {
        }

        @Override
        public void uv(long ptr, float u, float v) {
        }

        @Override
        public void light(long ptr, int light) {
        }

        @Override
        public void normal(long ptr, int normal) {
        }

        @Override
        public int getStride() {
            return 32;
        }
    }
}

