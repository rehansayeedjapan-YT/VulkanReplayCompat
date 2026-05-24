/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_5348
 */
package net.vulkanmod.config.gui.widget;

import net.minecraft.class_10799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5348;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.config.gui.util.VGuiConstants;
import net.vulkanmod.config.gui.widget.VAbstractWidget;
import net.vulkanmod.vulkan.util.ColorUtil;

public class ModIconWidget
extends VAbstractWidget {
    final class_5348 name;
    final class_2960 icon;

    public ModIconWidget(class_5348 name, class_2960 icon, int x0, int y0, int width, int height) {
        this.name = name;
        this.icon = icon;
        this.x = x0;
        this.y = y0;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(double mX, double mY) {
        int backgroundColor = ColorUtil.ARGB.multiplyAlpha(VGuiConstants.COLOR_BLACK, 0.6f);
        int width = this.width;
        int height = this.height;
        GuiRenderer.fill(this.x, this.y, this.x + width, this.y + height, backgroundColor);
        int size = this.height - 4;
        int iconX = this.x + 4;
        int iconY = this.y + (height - size) / 2;
        GuiRenderer.guiGraphics.method_25290(class_10799.field_56883, this.icon, iconX, iconY, 0.0f, 0.0f, size, size, size, size);
        size = this.height;
        GuiRenderer.drawString(class_310.method_1551().field_1772, (class_2561)this.name, this.x + 6 + size, this.y + this.height / 2 - 4, -1);
    }
}

