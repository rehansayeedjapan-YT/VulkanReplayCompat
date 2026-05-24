/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  net.minecraft.class_11231
 *  net.minecraft.class_11244
 *  net.minecraft.class_156
 *  net.minecraft.class_2561
 *  net.minecraft.class_287
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_4587
 *  net.minecraft.class_5348
 *  net.minecraft.class_5481
 *  org.joml.Matrix3x2f
 */
package net.vulkanmod.config.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.List;
import net.minecraft.class_11231;
import net.minecraft.class_11244;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_287;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import net.vulkanmod.config.gui.render.PolygonRenderState;
import org.joml.Matrix3x2f;

public abstract class GuiRenderer {
    public static class_310 minecraft;
    public static class_332 guiGraphics;
    public static class_4587 pose;
    public static class_287 bufferBuilder;

    public static void enableScissor(int i, int j, int k, int l) {
        guiGraphics.method_44379(i, j, k, l);
    }

    public static void disableScissor() {
        guiGraphics.method_44380();
    }

    public static void fillBox(int x0, int y0, int width, int height, int color) {
        GuiRenderer.fill(x0, y0, x0 + width, y0 + height, 0, color);
    }

    public static void fill(int x0, int y0, int x1, int y1, int color) {
        GuiRenderer.fill(x0, y0, x1, y1, 0, color);
    }

    public static void fill(int x0, int y0, int x1, int y1, int z, int color) {
        guiGraphics.method_25294(x0, y0, x1, y1, color);
    }

    public static void fillGradient(int x0, int y0, int x1, int y1, int color1, int color2) {
        GuiRenderer.fillGradient(x0, y0, x1, y1, 0, color1, color2);
    }

    public static void fillGradient(int x0, int y0, int x1, int y1, int z, int color1, int color2) {
        guiGraphics.method_25296(x0, y0, x1, y1, color1, color2);
    }

    public static void renderBoxBorder(int x0, int y0, int width, int height, int borderWidth, int color) {
        GuiRenderer.renderBorder(x0, y0, x0 + width, y0 + height, borderWidth, color);
    }

    public static void renderBorder(int x0, int y0, int x1, int y1, int width, int color) {
        GuiRenderer.fill(x0, y0, x1, y0 + width, color);
        GuiRenderer.fill(x0, y1 - width, x1, y1, color);
        GuiRenderer.fill(x0, y0 + width, x0 + width, y1 - width, color);
        GuiRenderer.fill(x1 - width, y0 + width, x1, y1 - width, color);
    }

    public static void drawString(class_327 font, class_2561 component, int x, int y, int color) {
        GuiRenderer.drawString(font, component.method_30937(), x, y, color);
    }

    public static void drawString(class_327 font, class_5481 formattedCharSequence, int x, int y, int color) {
        guiGraphics.method_35720(font, formattedCharSequence, x, y, color);
    }

    public static void drawString(class_327 font, class_2561 component, int x, int y, int color, boolean shadow) {
        GuiRenderer.drawString(font, component.method_30937(), x, y, color, shadow);
    }

    public static void drawString(class_327 font, class_5481 formattedCharSequence, int x, int y, int color, boolean shadow) {
        guiGraphics.method_51430(font, formattedCharSequence, x, y, color, shadow);
    }

    public static void drawCenteredString(class_327 font, class_2561 component, int x, int y, int color) {
        class_5481 formattedCharSequence = component.method_30937();
        guiGraphics.method_35720(font, formattedCharSequence, x - font.method_30880(formattedCharSequence) / 2, y, color);
    }

    public static void drawScrollingString(class_327 font, class_2561 component, int x, int y, int maxWidth, int color) {
        int textWidth = font.method_27525((class_5348)component);
        if (textWidth <= maxWidth) {
            GuiRenderer.drawCenteredString(font, component, x, y, color);
        } else {
            int x0 = x - maxWidth / 2;
            int x1 = x + maxWidth / 2;
            int scrollAmount = textWidth - maxWidth;
            double currentTimeInSeconds = (double)class_156.method_658() / 1000.0;
            double scrollSpeed = Math.max((double)scrollAmount * 0.5, 3.0);
            double scrollingOffset = Math.sin(1.5707963267948966 * Math.cos(Math.PI * 2 * currentTimeInSeconds / scrollSpeed)) / 2.0 + 0.5;
            double horizontalScroll = class_3532.method_16436((double)scrollingOffset, (double)0.0, (double)scrollAmount);
            GuiRenderer.enableScissor(x0 - 1, 0, x1, class_310.method_1551().method_22683().method_4507());
            GuiRenderer.drawString(font, component, (int)((double)x0 - horizontalScroll), y, color);
            GuiRenderer.disableScissor();
        }
    }

    public static int getMaxTextWidth(class_327 font, List<class_5481> list) {
        int maxWidth = 0;
        for (class_5481 text : list) {
            int width = font.method_30880(text);
            if (width <= maxWidth) continue;
            maxWidth = width;
        }
        return maxWidth;
    }

    public static void submitPolygon(RenderPipeline renderPipeline, class_11231 textureSetup, float[][] vertices, int color) {
        GuiRenderer.guiGraphics.field_59826.method_70919((class_11244)new PolygonRenderState(renderPipeline, textureSetup, new Matrix3x2f(), vertices, color, GuiRenderer.guiGraphics.field_44659.method_70863()));
    }
}

