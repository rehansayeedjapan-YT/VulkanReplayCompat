/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1144
 *  net.minecraft.class_11908
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_3532
 *  net.minecraft.class_5348
 */
package net.vulkanmod.config.gui.widget;

import net.minecraft.class_1144;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_3532;
import net.minecraft.class_5348;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.config.gui.widget.OptionWidget;
import net.vulkanmod.config.option.RangeOption;
import net.vulkanmod.vulkan.util.ColorUtil;

public class RangeOptionWidget
extends OptionWidget<RangeOption> {
    private boolean focused;

    public RangeOptionWidget(RangeOption option, class_2561 name) {
        super(option, name);
        this.setValue(option.getScaledValue());
    }

    @Override
    protected void renderControls(double mouseX, double mouseY) {
        float scaledValue = ((RangeOption)this.option).getScaledNewValue();
        int valueX = this.controlX + (int)(scaledValue * (float)this.controlWidth);
        if (this.controlHovered && this.active) {
            int halfWidth = 2;
            int halfHeight = 4;
            int y0 = (int)((float)this.y + (float)this.height * 0.5f - 1.0f);
            int y1 = (int)((float)y0 + 2.0f);
            GuiRenderer.fill(this.controlX, y0, this.controlX + this.controlWidth, y1, ColorUtil.ARGB.pack(1.0f, 1.0f, 1.0f, 0.1f));
            GuiRenderer.fill(this.controlX, y0, valueX - halfWidth, y1, ColorUtil.ARGB.pack(1.0f, 1.0f, 1.0f, 0.3f));
            int color = ColorUtil.ARGB.pack(1.0f, 1.0f, 1.0f, 0.3f);
            GuiRenderer.renderBorder(valueX - halfWidth, y0 - halfHeight, valueX + halfWidth, y1 + halfHeight, 1, color);
        } else {
            int y0 = (int)((float)(this.y + this.height) - 5.0f);
            int y1 = (int)((float)y0 + 1.5f);
            GuiRenderer.fill(this.controlX, y0, this.controlX + this.controlWidth, y1, ColorUtil.ARGB.pack(1.0f, 1.0f, 1.0f, 0.3f));
            float alpha = this.active ? 0.8f : 0.3f;
            GuiRenderer.fill(this.controlX, y0, valueX, y1, ColorUtil.ARGB.pack(1.0f, 1.0f, 1.0f, alpha));
        }
        int color = this.active ? -1 : -6250336;
        class_327 font = class_310.method_1551().field_1772;
        class_2561 text = this.getDisplayedValue();
        int width = font.method_27525((class_5348)text);
        int x = this.controlX + this.controlWidth / 2 - width / 2;
        int y = this.y + (this.height - 9) / 2;
        GuiRenderer.drawString(font, text.method_30937(), x, y, color);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.setValueFromMouse(mouseX);
    }

    public boolean method_25404(class_11908 event) {
        boolean isRight;
        boolean isLeft = event.comp_4795() == 263;
        boolean bl = isRight = event.comp_4795() == 262;
        if (isLeft || isRight) {
            float direction = isLeft ? -1.0f : 1.0f;
            double currentValue = ((RangeOption)this.option).getScaledValue();
            this.setValue(currentValue + (double)(direction / (float)(this.width - 8)));
        }
        return false;
    }

    @Override
    public void method_25365(boolean bl) {
        this.focused = bl;
    }

    @Override
    public boolean method_25370() {
        return this.focused;
    }

    private void setValueFromMouse(double mouseX) {
        this.setValue((mouseX - (double)(this.controlX + 4)) / (double)(this.controlWidth - 8));
    }

    private void setValue(double value) {
        double currentValue = ((RangeOption)this.option).getScaledValue();
        if (currentValue != (value = class_3532.method_15350((double)value, (double)0.0, (double)1.0))) {
            this.applyNewValue((float)value);
        }
        this.updateDisplayedValue();
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.setValueFromMouse(mouseX);
    }

    private void applyNewValue(float value) {
        ((RangeOption)this.option).setNewValueFromScaledFloat(value);
    }

    @Override
    public void playDownSound(class_1144 soundManager) {
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (this.controlHovered) {
            super.playDownSound(class_310.method_1551().method_1483());
        }
    }
}

