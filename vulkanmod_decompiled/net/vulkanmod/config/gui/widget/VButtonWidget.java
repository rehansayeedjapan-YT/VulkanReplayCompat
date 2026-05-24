/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_8016
 *  net.minecraft.class_8023
 *  org.jetbrains.annotations.Nullable
 */
package net.vulkanmod.config.gui.widget;

import java.util.function.Consumer;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_8016;
import net.minecraft.class_8023;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.config.gui.util.VGuiConstants;
import net.vulkanmod.config.gui.widget.VAbstractWidget;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.jetbrains.annotations.Nullable;

public class VButtonWidget
extends VAbstractWidget {
    boolean selected = false;
    Consumer<VButtonWidget> onPress;
    float alpha = 1.0f;

    public VButtonWidget(int x, int y, int width, int height, class_2561 message, Consumer<VButtonWidget> onPress) {
        this.setPosition(x, y, width, height);
        this.message = message;
        this.onPress = onPress;
    }

    @Override
    public void renderWidget(double mouseX, double mouseY) {
        if (!this.isVisible()) {
            return;
        }
        int backgroundColor = this.method_37303() ? ColorUtil.ARGB.multiplyAlpha(VGuiConstants.COLOR_BLACK, 0.45f) : ColorUtil.ARGB.multiplyAlpha(VGuiConstants.COLOR_BLACK, 0.3f);
        int textColor = this.method_37303() ? VGuiConstants.COLOR_WHITE : VGuiConstants.COLOR_GRAY;
        int selectionOutlineColor = ColorUtil.ARGB.multiplyAlpha(VGuiConstants.COLOR_RED, 0.8f);
        int selectionFillColor = ColorUtil.ARGB.multiplyAlpha(VGuiConstants.COLOR_RED, 0.2f);
        GuiRenderer.fill(this.x, this.y, this.x + this.width, this.y + this.height, backgroundColor);
        if (this.selected) {
            GuiRenderer.fill(this.x, this.y, this.x + 2, this.y + this.height, selectionOutlineColor);
            GuiRenderer.fill(this.x, this.y, this.x + this.width, this.y + this.height, selectionFillColor);
        }
        this.renderHovering(0, 0);
        if (this.centeredText) {
            GuiRenderer.drawCenteredString(class_310.method_1551().field_1772, this.message, this.x + this.width / 2, this.y + this.height / 2 - 4, textColor | class_3532.method_15386((float)(this.alpha * 255.0f)) << 24);
        } else {
            GuiRenderer.drawString(class_310.method_1551().field_1772, this.message, this.x + this.margin, this.y + this.height / 2 - 4, textColor | class_3532.method_15386((float)(this.alpha * 255.0f)) << 24);
        }
    }

    @Override
    public void onClick(double mX, double mY) {
        this.onPress.accept(this);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean method_37303() {
        return this.active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    @Nullable
    public class_8016 method_48205(class_8023 event) {
        if (!this.active || !this.visible) {
            return null;
        }
        return super.method_48205(event);
    }
}

