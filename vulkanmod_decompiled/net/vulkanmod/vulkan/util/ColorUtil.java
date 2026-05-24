/*
 * Decompiled with CFR 0.152.
 */
package net.vulkanmod.vulkan.util;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import net.vulkanmod.vulkan.util.MappedBuffer;

public class ColorUtil {
    private static final float COLOR_INV = 0.003921569f;
    static ColorConsumer colorConsumer = new DefaultColorConsumer();

    public static void useGammaCorrection(boolean b) {
        colorConsumer = b ? new GammaColorConsumer() : new DefaultColorConsumer();
    }

    public static int floatToInt(float f) {
        return (int)(f * 255.0f) & 0xFF;
    }

    public static float unpackColor(int c, int s) {
        return (float)(c >> s & 0xFF) * 0.003921569f;
    }

    public static int BGRAtoRGBA(int v) {
        byte r = (byte)(v >> 16);
        byte b = (byte)v;
        return r & 0xFF | b << 16 & 0xFF0000 | v & 0xFF00FF00;
    }

    public static float gamma(float f) {
        return (float)Math.pow(f, 2.2);
    }

    public static void setRGBA_Buffer(MappedBuffer buffer, float r, float g, float b, float a) {
        colorConsumer.setRGBA_Buffer(buffer, r, g, b, a);
    }

    public static void setRGBA_Buffer(FloatBuffer buffer, float r, float g, float b, float a) {
        colorConsumer.setRGBA_Buffer(buffer, r, g, b, a);
    }

    public static class GammaColorConsumer
    implements ColorConsumer {
        @Override
        public void setRGBA_Buffer(MappedBuffer buffer, float r, float g, float b, float a) {
            r = ColorUtil.gamma(r);
            g = ColorUtil.gamma(g);
            b = ColorUtil.gamma(b);
            this.putColor(buffer, r, g, b, a);
        }

        @Override
        public void setRGBA_Buffer(FloatBuffer buffer, float r, float g, float b, float a) {
            r = ColorUtil.gamma(r);
            g = ColorUtil.gamma(g);
            b = ColorUtil.gamma(b);
            this.putColor(buffer, r, g, b, a);
        }

        @Override
        public void setRGBA_Buffer(ByteBuffer buffer, float r, float g, float b, float a) {
            r = ColorUtil.gamma(r);
            g = ColorUtil.gamma(g);
            b = ColorUtil.gamma(b);
            this.putColor(buffer, r, g, b, a);
        }
    }

    public static class DefaultColorConsumer
    implements ColorConsumer {
        @Override
        public void setRGBA_Buffer(MappedBuffer buffer, float r, float g, float b, float a) {
            this.putColor(buffer, r, g, b, a);
        }

        @Override
        public void setRGBA_Buffer(FloatBuffer buffer, float r, float g, float b, float a) {
            this.putColor(buffer, r, g, b, a);
        }

        @Override
        public void setRGBA_Buffer(ByteBuffer buffer, float r, float g, float b, float a) {
            this.putColor(buffer, r, g, b, a);
        }
    }

    static interface ColorConsumer {
        public void setRGBA_Buffer(MappedBuffer var1, float var2, float var3, float var4, float var5);

        public void setRGBA_Buffer(FloatBuffer var1, float var2, float var3, float var4, float var5);

        public void setRGBA_Buffer(ByteBuffer var1, float var2, float var3, float var4, float var5);

        default public void putColor(MappedBuffer buffer, float r, float g, float b, float a) {
            buffer.putFloat(0, r);
            buffer.putFloat(4, g);
            buffer.putFloat(8, b);
            buffer.putFloat(12, a);
        }

        default public void putColor(FloatBuffer buffer, float r, float g, float b, float a) {
            buffer.put(0, r);
            buffer.put(1, g);
            buffer.put(2, b);
            buffer.put(3, a);
        }

        default public void putColor(ByteBuffer buffer, float r, float g, float b, float a) {
            buffer.putFloat(0, r);
            buffer.putFloat(4, g);
            buffer.putFloat(8, b);
            buffer.putFloat(12, a);
        }
    }

    public static class RGBA {
        public static int pack(float r, float g, float b, float a) {
            int color = ColorUtil.floatToInt(a) << 24 | ColorUtil.floatToInt(b) << 16 | ColorUtil.floatToInt(g) << 8 | ColorUtil.floatToInt(r);
            return color;
        }

        public static float unpackR(int color) {
            return ColorUtil.unpackColor(color, 24);
        }

        public static float unpackG(int color) {
            return ColorUtil.unpackColor(color, 16);
        }

        public static float unpackB(int color) {
            return ColorUtil.unpackColor(color, 8);
        }

        public static int fromArgb32(int i) {
            return i & 0xFF00FF00 | (i & 0xFF0000) >> 16 | (i & 0xFF) << 16;
        }
    }

    public static class ARGB {
        public static int pack(float r, float g, float b, float a) {
            int color = ColorUtil.floatToInt(a) << 24 | ColorUtil.floatToInt(r) << 16 | ColorUtil.floatToInt(g) << 8 | ColorUtil.floatToInt(b);
            return color;
        }

        public static float unpackR(int color) {
            return ColorUtil.unpackColor(color, 16);
        }

        public static float unpackG(int color) {
            return ColorUtil.unpackColor(color, 8);
        }

        public static float unpackB(int color) {
            return ColorUtil.unpackColor(color, 0);
        }

        public static float unpackA(int color) {
            return ColorUtil.unpackColor(color, 24);
        }

        public static int multiplyAlpha(int color, float m) {
            int newA = ColorUtil.floatToInt(ARGB.unpackA(color) * m);
            return color & 0xFFFFFF | newA << 24;
        }

        public static int multiplyRGB(int color, float m) {
            int alpha = color >>> 24 & 0xFF;
            int red = (int)((float)(color >>> 16 & 0xFF) * m);
            int green = (int)((float)(color >>> 8 & 0xFF) * m);
            int blue = (int)((float)(color & 0xFF) * m);
            return alpha << 24 | red << 16 | green << 8 | blue;
        }

        public static int toRGBA(int color) {
            return color & 0xFF00FF00 | color >> 16 & 0xFF | color << 16 & 0xFF0000;
        }
    }
}

