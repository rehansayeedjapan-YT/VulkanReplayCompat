/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_156
 *  net.minecraft.class_364
 *  net.minecraft.class_6379
 *  net.minecraft.class_6379$class_6380
 *  net.minecraft.class_6382
 *  net.minecraft.class_8016
 *  net.minecraft.class_8023
 *  net.minecraft.class_8030
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package net.vulkanmod.config.gui;

import net.minecraft.class_156;
import net.minecraft.class_364;
import net.minecraft.class_6379;
import net.minecraft.class_6382;
import net.minecraft.class_8016;
import net.minecraft.class_8023;
import net.minecraft.class_8030;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiElement
implements class_364,
class_6379 {
    protected int width;
    protected int height;
    public int x;
    public int y;
    protected boolean hovered;
    protected long hoverStartTime;
    protected int hoverTime;
    protected long hoverStopTime;

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setPosition(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void updateState(double mX, double mY) {
        if (this.method_25405(mX, mY)) {
            if (!this.hovered) {
                this.hoverStartTime = class_156.method_658();
            }
            this.hovered = true;
            this.hoverTime = (int)(class_156.method_658() - this.hoverStartTime);
        } else {
            if (this.hovered) {
                this.hoverStopTime = class_156.method_658();
            }
            this.hovered = false;
            this.hoverTime = 0;
        }
    }

    public float getHoverMultiplier(float time) {
        if (this.hovered) {
            return Math.min((float)this.hoverTime / time, 1.0f);
        }
        int delta = (int)(class_156.method_658() - this.hoverStopTime);
        return Math.max(1.0f - (float)delta / time, 0.0f);
    }

    @Nullable
    public class_8016 method_48205(class_8023 focusNavigationEvent) {
        return super.method_48205(focusNavigationEvent);
    }

    public boolean method_25405(double mouseX, double mouseY) {
        return mouseX >= (double)this.x && mouseY >= (double)this.y && mouseX <= (double)(this.x + this.width) && mouseY <= (double)(this.y + this.height);
    }

    @Nullable
    public class_8016 method_48218() {
        return super.method_48218();
    }

    @NotNull
    public class_8030 method_48202() {
        return super.method_48202();
    }

    public void method_25365(boolean bl) {
    }

    public boolean method_25370() {
        return false;
    }

    @NotNull
    public class_6379.class_6380 method_37018() {
        return class_6379.class_6380.field_33784;
    }

    public void method_37020(class_6382 narrationElementOutput) {
    }
}

