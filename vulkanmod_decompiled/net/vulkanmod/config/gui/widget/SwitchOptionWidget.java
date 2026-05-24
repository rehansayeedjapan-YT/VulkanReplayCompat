/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 */
package net.vulkanmod.config.gui.widget;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.config.gui.widget.OptionWidget;
import net.vulkanmod.config.option.SwitchOption;
import net.vulkanmod.vulkan.util.ColorUtil;

public class SwitchOptionWidget
extends OptionWidget<SwitchOption> {
    private boolean focused;

    public SwitchOptionWidget(SwitchOption option, class_2561 name) {
        super(option, name);
        this.updateDisplayedValue();
    }

    @Override
    protected void renderControls(double mouseX, double mouseY) {
        int color;
        int center = this.controlX + this.controlWidth / 2;
        int halfWidth = 12;
        int x0 = center - halfWidth;
        int y0 = this.y + 4;
        int height = this.height - 8;
        int w1 = halfWidth - 4;
        int h1 = height - 4;
        if (((Boolean)((SwitchOption)this.option).getNewValue()).booleanValue()) {
            int x1 = x0 + halfWidth + 2;
            color = ColorUtil.ARGB.pack(0.4f, 0.4f, 0.4f, 1.0f);
            GuiRenderer.fillBox(x0 + 2, y0 + 2, x1 - (x0 + 2) - 1, h1, color);
            color = ColorUtil.ARGB.pack(1.0f, 1.0f, 1.0f, 1.0f);
            GuiRenderer.fillBox(x1, y0 + 2, w1, h1, color);
        } else {
            color = ColorUtil.ARGB.pack(1.0f, 1.0f, 1.0f, 0.4f);
            GuiRenderer.fillBox(x0 + 2, y0 + 2, w1, h1, color);
        }
        color = ColorUtil.ARGB.pack(0.6f, 0.6f, 0.6f, 1.0f);
        GuiRenderer.renderBoxBorder(x0, y0, halfWidth * 2, height, 1, color);
        color = this.active ? -1 : -6250336;
        class_327 textRenderer = class_310.method_1551().field_1772;
        int margin = Math.max(textRenderer.method_1727(class_2561.method_43471((String)"options.on").getString()) / 3, textRenderer.method_1727(class_2561.method_43471((String)"options.off").getString()) / 3);
        int x = this.controlX + this.controlWidth / 2 - (int)((float)halfWidth * 1.5f) - 4 - margin;
        int y = this.y + (this.height - 8) / 2;
        GuiRenderer.drawCenteredString(textRenderer, this.getDisplayedValue(), x, y, color);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        ((SwitchOption)this.option).setNewValue((Boolean)((SwitchOption)this.option).getNewValue() == false);
        this.updateDisplayedValue();
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
    }

    @Override
    protected void updateDisplayedValue() {
        this.displayedValue = (Boolean)((SwitchOption)this.option).getNewValue() != false ? class_2561.method_43471((String)"options.on") : class_2561.method_43471((String)"options.off");
    }

    @Override
    public void method_25365(boolean bl) {
        this.focused = bl;
    }

    @Override
    public boolean method_25370() {
        return this.focused;
    }
}

