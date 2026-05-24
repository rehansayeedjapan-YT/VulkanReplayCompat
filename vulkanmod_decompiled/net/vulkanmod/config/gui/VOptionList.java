/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.opengl.GlStateManager
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_364
 *  org.jetbrains.annotations.Nullable
 */
package net.vulkanmod.config.gui;

import com.mojang.blaze3d.opengl.GlStateManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_364;
import net.vulkanmod.config.gui.GuiElement;
import net.vulkanmod.config.gui.OptionBlock;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.config.gui.widget.OptionWidget;
import net.vulkanmod.config.gui.widget.VAbstractWidget;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.jetbrains.annotations.Nullable;

public class VOptionList
extends GuiElement {
    private final List<Entry> children = new ObjectArrayList();
    boolean scrolling = false;
    float scrollAmount = 0.0f;
    int itemWidth;
    int totalItemHeight;
    int itemHeight;
    int itemMargin;
    int listLength = 0;
    Entry focused;

    public VOptionList(int x, int y, int width, int height, int itemHeight) {
        this.setPosition(x, y, width, height);
        this.width = width;
        this.height = height;
        this.itemWidth = this.width - 7;
        this.itemHeight = itemHeight;
        this.itemMargin = 3;
        this.totalItemHeight = this.itemHeight + this.itemMargin;
    }

    public void addButton(OptionWidget<?> widget) {
        this.addEntry(new Entry(widget, this.itemMargin, null));
    }

    public void addAll(OptionBlock[] blocks) {
        for (OptionBlock block : blocks) {
            Option<?>[] options;
            int x0 = this.x;
            int width = this.itemWidth;
            int height = this.itemHeight;
            String title = block.title();
            if (title != null && !title.isEmpty()) {
                this.addEntry(new Entry(null, 8, title));
            }
            for (Option<?> option : options = block.options()) {
                int margin = this.itemMargin;
                OptionWidget<?> widget = option.getWidget();
                widget.setDimensions(x0, 0, width, height);
                this.addEntry(new Entry(widget, margin, null));
            }
            this.addEntry(new Entry(null, 12, null));
        }
    }

    private void addEntry(Entry entry) {
        this.children.add(entry);
        this.listLength += entry.getTotalHeight();
    }

    public void clearEntries() {
        this.listLength = 0;
        this.children.clear();
    }

    protected void updateScrollingState(double mouseX, int button) {
        this.scrolling = button == 0 && mouseX >= (double)this.getScrollbarPosition() && mouseX < (double)(this.getScrollbarPosition() + 6);
    }

    protected float getScrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(double d) {
        this.scrollAmount = (float)class_3532.method_15350((double)d, (double)0.0, (double)this.getMaxScroll());
    }

    private int getItemCount() {
        return this.children.size();
    }

    class_364 getFocused() {
        return this.focused;
    }

    void setFocused(Entry focussed) {
        this.focused = focussed;
    }

    public boolean method_25402(class_11909 event, boolean bl) {
        this.updateScrollingState(event.comp_4798(), event.method_74245());
        if (this.method_25405(event.comp_4798(), event.comp_4799())) {
            Entry entry = this.getEntryAtPos(event.comp_4798(), event.comp_4799());
            if (entry != null && entry.method_25402(event, bl)) {
                this.setFocused(entry);
                entry.method_25365(true);
                return true;
            }
            return event.method_74245() == 0;
        }
        return false;
    }

    public boolean method_25406(class_11909 event) {
        Entry entry;
        if (this.isValidClickButton(event.method_74245()) && (entry = this.getEntryAtPos(event.comp_4798(), event.comp_4799())) != null && entry.method_25406(event)) {
            entry.method_25365(false);
            this.setFocused(null);
            return true;
        }
        return false;
    }

    public boolean method_25403(class_11909 event, double deltaX, double deltaY) {
        if (event.method_74245() != 0) {
            return false;
        }
        if (this.getFocused() != null) {
            return this.getFocused().method_25403(event, deltaX, deltaY);
        }
        if (!this.scrolling) {
            return false;
        }
        double maxScroll = this.getMaxScroll();
        if (event.comp_4799() < (double)this.y) {
            this.setScrollAmount(0.0);
        } else if (event.comp_4799() > (double)this.getBottom()) {
            this.setScrollAmount(maxScroll);
        } else if (maxScroll > 0.0) {
            double barHeight = (double)this.height * (double)this.height / (double)this.getTotalLength();
            double scrollFactor = Math.max(1.0, maxScroll / ((double)this.height - barHeight));
            this.setScrollAmount((double)this.getScrollAmount() + deltaY * scrollFactor);
        }
        return true;
    }

    public boolean method_25401(double mouseX, double mouseY, double xScroll, double yScroll) {
        this.setScrollAmount((double)this.getScrollAmount() - yScroll * (double)this.totalItemHeight / 2.0);
        return true;
    }

    public int getMaxScroll() {
        return Math.max(0, this.getTotalLength() - this.height);
    }

    protected int getTotalLength() {
        return this.listLength;
    }

    public int getBottom() {
        return this.y + this.height;
    }

    @Nullable
    protected Entry getEntryAtPos(double x, double y) {
        int x0 = this.x;
        if (x > (double)this.getScrollbarPosition() || x < (double)x0) {
            return null;
        }
        for (Entry entry : this.children) {
            VAbstractWidget widget = entry.widget;
            if (widget == null || !(y >= (double)widget.y) || !(y <= (double)(widget.y + widget.height))) continue;
            return entry;
        }
        return null;
    }

    @Override
    public void updateState(double mX, double mY) {
        if (this.focused != null) {
            return;
        }
        super.updateState(mX, mY);
    }

    public void renderWidget(int mouseX, int mouseY) {
        GuiRenderer.enableScissor(this.x, this.y, this.x + this.width, this.y + this.height);
        this.renderList(mouseX, mouseY);
        GuiRenderer.disableScissor();
        int maxScroll = this.getMaxScroll();
        if (maxScroll > 0) {
            GlStateManager._enableBlend();
            int height = this.getHeight();
            int totalLength = this.getTotalLength();
            int barHeight = (int)((float)(height * height) / (float)totalLength);
            barHeight = class_3532.method_15340((int)barHeight, (int)32, (int)(height - 8));
            int scrollAmount = (int)this.getScrollAmount();
            int barY = scrollAmount * (height - barHeight) / maxScroll + this.getY();
            barY = Math.max(barY, this.getY());
            int scrollbarPosition = this.getScrollbarPosition();
            int thickness = 3;
            int backgroundColor = ColorUtil.ARGB.pack(0.8f, 0.8f, 0.8f, 0.2f);
            GuiRenderer.fill(scrollbarPosition, this.getY(), scrollbarPosition + thickness, this.getY() + height, backgroundColor);
            int barColor = ColorUtil.ARGB.pack(0.3f, 0.0f, 0.0f, 0.6f);
            GuiRenderer.fill(scrollbarPosition, barY, scrollbarPosition + thickness, barY + barHeight, barColor);
        }
    }

    protected int getScrollbarPosition() {
        return this.x + this.width;
    }

    public VAbstractWidget getHoveredWidget(double mouseX, double mouseY) {
        if (this.focused != null) {
            return this.focused.widget;
        }
        if (!this.method_25405(mouseX, mouseY)) {
            return null;
        }
        for (Entry entry : this.children) {
            VAbstractWidget widget = entry.widget;
            if (widget == null || !widget.method_25405(mouseX, mouseY)) continue;
            return widget;
        }
        return null;
    }

    protected void renderList(int mouseX, int mouseY) {
        int itemCount = this.getItemCount();
        int rowTop = this.y - (int)this.getScrollAmount();
        for (int j = 0; j < itemCount; ++j) {
            Entry entry = this.getEntry(j);
            if (rowTop + entry.getTotalHeight() >= this.y && rowTop <= this.y + this.height) {
                boolean updateState = this.focused == null;
                entry.render(rowTop, mouseX, mouseY, updateState, this.x);
            }
            rowTop += entry.getTotalHeight();
        }
    }

    private Entry getEntry(int j) {
        return this.children.get(j);
    }

    protected boolean isValidClickButton(int i) {
        return i == 0;
    }

    protected static class Entry
    implements class_364 {
        final VAbstractWidget widget;
        final int margin;
        final String headerTitle;

        private Entry(OptionWidget<?> widget, int margin, String headerTitle) {
            this.widget = widget;
            this.margin = margin;
            this.headerTitle = headerTitle;
        }

        public void render(int y, int mouseX, int mouseY, boolean updateState, int listX) {
            if (this.headerTitle != null && !this.headerTitle.isEmpty()) {
                int headerY = y + 4;
                GuiRenderer.drawString(class_310.method_1551().field_1772, (class_2561)class_2561.method_43470((String)this.headerTitle), listX + 8, headerY, -1);
                return;
            }
            if (this.widget == null) {
                return;
            }
            this.widget.y = y;
            if (updateState) {
                this.widget.updateState(mouseX, mouseY);
            }
            this.widget.render(mouseX, mouseY);
        }

        public int getTotalHeight() {
            if (this.headerTitle != null && !this.headerTitle.isEmpty()) {
                Objects.requireNonNull(class_310.method_1551().field_1772);
                return 9 + this.margin;
            }
            if (this.widget != null) {
                return this.widget.height + this.margin;
            }
            return this.margin;
        }

        public boolean method_25402(class_11909 event, boolean bl) {
            if (this.widget == null) {
                return false;
            }
            return this.widget.method_25402(event, bl);
        }

        public boolean method_25406(class_11909 event) {
            if (this.widget == null) {
                return false;
            }
            return this.widget.method_25406(event);
        }

        public boolean method_25403(class_11909 event, double deltaX, double deltaY) {
            if (this.widget == null) {
                return false;
            }
            return this.widget.method_25403(event, deltaX, deltaY);
        }

        public boolean method_25370() {
            return false;
        }

        public void method_25365(boolean bl) {
            if (this.widget != null) {
                this.widget.method_25365(bl);
            }
        }
    }
}

