/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11231
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  org.jetbrains.annotations.NotNull
 */
package net.vulkanmod.config.gui.widget;

import net.minecraft.class_11231;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.config.gui.widget.OptionWidget;
import net.vulkanmod.config.option.CyclingOption;
import net.vulkanmod.render.shader.CustomRenderPipelines;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.jetbrains.annotations.NotNull;

public class CyclingOptionWidget
extends OptionWidget<CyclingOption<?>> {
    private final Button leftButton = new Button(Button.Direction.LEFT);
    private final Button rightButton = new Button(Button.Direction.RIGHT);
    private boolean focused;

    public CyclingOptionWidget(CyclingOption<?> option, class_2561 name) {
        super(option, name);
    }

    @Override
    public void setDimensions(int x, int y, int width, int height) {
        super.setDimensions(x, y, width, height);
        this.leftButton.setDimensions(this.controlX, 16);
        this.rightButton.setDimensions(this.controlX + this.controlWidth - 16, 16);
    }

    @Override
    public void renderControls(double mouseX, double mouseY) {
        this.renderBars();
        this.leftButton.setStatus(((CyclingOption)this.option).index() > 0);
        this.rightButton.setStatus(((CyclingOption)this.option).index() < ((CyclingOption)this.option).getValues().length - 1);
        int color = this.active ? -1 : -6250336;
        class_327 textRenderer = class_310.method_1551().field_1772;
        int x = this.controlX + this.controlWidth / 2;
        int y = this.y + (this.height - 9) / 2;
        GuiRenderer.drawScrollingString(textRenderer, this.getDisplayedValue(), x, y, this.rightButton.x - (this.leftButton.x + this.leftButton.width) - 12, color);
        this.leftButton.renderButton(mouseX, mouseY);
        this.rightButton.renderButton(mouseX, mouseY);
    }

    public void renderBars() {
        int count = ((CyclingOption)this.option).getValues().length;
        int current = ((CyclingOption)this.option).index();
        int margin = 30;
        int padding = 4;
        int barWidth = (this.controlWidth - 2 * margin - padding * count) / count;
        int color = ColorUtil.ARGB.pack(1.0f, 1.0f, 1.0f, 0.4f);
        int activeColor = ColorUtil.ARGB.pack(1.0f, 1.0f, 1.0f, 1.0f);
        if (barWidth <= 0) {
            return;
        }
        for (int i = 0; i < count; ++i) {
            int x0 = this.controlX + margin + i * (barWidth + padding);
            int y0 = this.y + this.height - 5;
            int c = i == current ? activeColor : color;
            GuiRenderer.fill(x0, y0, x0 + barWidth, (int)((float)y0 + 1.5f), c);
        }
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
        this.leftButton.active &= active;
        this.rightButton.active &= active;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (this.leftButton.isHovered(mouseX, mouseY)) {
            ((CyclingOption)this.option).prevValue();
        } else if (this.rightButton.isHovered(mouseX, mouseY)) {
            ((CyclingOption)this.option).nextValue();
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
    }

    @Override
    public void method_25365(boolean bl) {
        this.focused = bl;
    }

    @Override
    public boolean method_25370() {
        return this.focused;
    }

    class Button {
        final int ACTIVE_COLOR = ColorUtil.ARGB.pack(1.0f, 1.0f, 1.0f, 0.8f);
        final int HOVERED_COLOR = ColorUtil.ARGB.pack(1.0f, 1.0f, 1.0f, 1.0f);
        final int INACTIVE_COLOR = ColorUtil.ARGB.pack(0.3f, 0.3f, 0.3f, 0.8f);
        int x;
        int width;
        boolean active = true;
        Direction direction;

        Button(Direction direction) {
            this.direction = direction;
        }

        public void setDimensions(int x, int width) {
            this.x = x;
            this.width = width;
        }

        boolean isHovered(double mouseX, double mouseY) {
            return mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)CyclingOptionWidget.this.y && mouseY <= (double)(CyclingOptionWidget.this.y + CyclingOptionWidget.this.height);
        }

        void setStatus(boolean status) {
            this.active = status;
        }

        void renderButton(double mouseX, double mouseY) {
            float f;
            float f2 = f = this.isHovered(mouseX, mouseY) && this.active ? 5.0f : 4.5f;
            int color = this.isHovered(mouseX, mouseY) && this.active ? this.HOVERED_COLOR : (this.active ? this.ACTIVE_COLOR : this.INACTIVE_COLOR);
            float[][] vertices = this.getVertices(f);
            GuiRenderer.submitPolygon(CustomRenderPipelines.GUI_TRIANGLES, class_11231.method_70899(), vertices, color);
        }

        private float[] @NotNull [] getVertices(float f) {
            float w = f - 1.0f;
            float yC = (float)CyclingOptionWidget.this.y + (float)CyclingOptionWidget.this.height * 0.5f;
            float xC = (float)this.x + (float)this.width * 0.5f;
            float[][] vertices = this.direction == Direction.LEFT ? new float[][]{{xC - w, yC}, {xC + w, yC + f}, {xC + w, yC - f}} : new float[][]{{xC + w, yC}, {xC - w, yC - f}, {xC - w, yC + f}};
            return vertices;
        }

        static enum Direction {
            LEFT,
            RIGHT;

        }
    }
}

