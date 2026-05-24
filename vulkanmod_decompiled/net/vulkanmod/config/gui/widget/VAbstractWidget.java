/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_1144
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_3417
 *  net.minecraft.class_6880
 */
package net.vulkanmod.config.gui.widget;

import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_1144;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3417;
import net.minecraft.class_6880;
import net.vulkanmod.config.gui.GuiElement;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.config.gui.util.VGuiConstants;
import net.vulkanmod.config.option.PerformanceImpact;
import net.vulkanmod.vulkan.util.ColorUtil;

public abstract class VAbstractWidget
extends GuiElement {
    public boolean active = true;
    public boolean visible = true;
    public boolean focused;
    protected class_2561 message;
    protected boolean centeredText = true;
    protected int margin = 4;

    public void setDimensions(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setTextLayout(boolean centered, int margin) {
        this.centeredText = centered;
        this.margin = margin;
    }

    public void render(double mX, double mY) {
        this.updateState(mX, mY);
        this.renderWidget(mX, mY);
    }

    public void renderWidget(double mX, double mY) {
    }

    public void onClick(double mX, double mY) {
    }

    public void onRelease(double mX, double mY) {
    }

    protected void onDrag(double mX, double mY, double f, double g) {
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    protected void renderHovering(int xPadding, int yPadding) {
        if (this.method_25370() || !this.method_37303() || !this.visible || this.focused) {
            return;
        }
        float hoverMultiplier = this.getHoverMultiplier(200.0f);
        int borderColor = ColorUtil.ARGB.multiplyAlpha(VGuiConstants.COLOR_RED, hoverMultiplier);
        int backgroundColor = ColorUtil.ARGB.multiplyAlpha(VGuiConstants.COLOR_RED, 0.3f * hoverMultiplier);
        if (hoverMultiplier > 0.0f) {
            GuiRenderer.fill(this.x - xPadding, this.y - yPadding, this.x + this.width + xPadding, this.y + this.height + yPadding, backgroundColor);
            int x0 = this.x - xPadding;
            int x1 = this.x + this.width + xPadding;
            int y0 = this.y - yPadding;
            int y1 = this.y + this.height + yPadding;
            int border = 1;
            GuiRenderer.renderBorder(x0, y0, x1, y1, border, borderColor);
        }
    }

    public boolean method_25402(class_11909 event, boolean bl) {
        boolean clicked;
        if (this.active && this.visible && this.isValidClickButton(event.method_74245()) && (clicked = this.clicked(event.comp_4798(), event.comp_4799()))) {
            this.playDownSound(class_310.method_1551().method_1483());
            this.onClick(event.comp_4798(), event.comp_4799());
            return true;
        }
        return false;
    }

    protected boolean clicked(double mX, double mY) {
        return this.active && this.visible && mX >= (double)this.getX() && mY >= (double)this.getY() && mX < (double)(this.getX() + this.getWidth()) && mY < (double)(this.getY() + this.getHeight());
    }

    public boolean method_25406(class_11909 event) {
        if (this.isValidClickButton(event.method_74245())) {
            this.onRelease(event.comp_4798(), event.comp_4799());
            return true;
        }
        return false;
    }

    protected boolean isValidClickButton(int button) {
        return button == 0;
    }

    public boolean method_25403(class_11909 event, double d, double e) {
        if (this.isValidClickButton(event.method_74245())) {
            this.onDrag(event.comp_4798(), event.comp_4799(), d, e);
            return true;
        }
        return false;
    }

    @Override
    public void updateState(double mX, double mY) {
        super.updateState(mX, mY);
    }

    public void playDownSound(class_1144 soundManager) {
        soundManager.method_4873((class_1113)class_1109.method_47978((class_6880)class_3417.field_15015, (float)1.0f));
    }

    public class_2561 getTooltip() {
        return null;
    }

    public PerformanceImpact getImpact() {
        return null;
    }
}

