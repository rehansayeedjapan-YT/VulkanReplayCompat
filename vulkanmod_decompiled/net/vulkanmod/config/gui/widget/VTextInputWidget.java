/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_156
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_8016
 *  net.minecraft.class_8023
 *  org.jetbrains.annotations.Nullable
 *  org.lwjgl.glfw.GLFW
 */
package net.vulkanmod.config.gui.widget;

import java.util.function.Consumer;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_8016;
import net.minecraft.class_8023;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.config.gui.util.VGuiConstants;
import net.vulkanmod.config.gui.widget.VAbstractWidget;
import net.vulkanmod.vulkan.util.ColorUtil;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class VTextInputWidget
extends VAbstractWidget {
    public boolean selected = false;
    Consumer<VTextInputWidget> onSearch;
    private String text;
    private final class_2561 placeholder;
    private int cursorPos = 0;
    private int selectionEnd = 0;
    private long lastBlinkTime = 0L;
    private boolean showCursor = true;
    private static final int CURSOR_BLINK_INTERVAL = 500;

    public VTextInputWidget(int x, int y, int width, int height, class_2561 placeholder, Consumer<VTextInputWidget> onSearch) {
        this.setPosition(x, y, width, height);
        this.placeholder = placeholder;
        this.onSearch = onSearch;
        this.text = "";
    }

    @Override
    public void renderWidget(double mouseX, double mouseY) {
        if (!this.isVisible()) {
            return;
        }
        boolean hasText = !this.text.isEmpty();
        boolean isFocused = this.focused || this.selected;
        int backgroundColor = ColorUtil.ARGB.multiplyAlpha(VGuiConstants.COLOR_BLACK, 0.45f);
        int textColor = hasText ? VGuiConstants.COLOR_WHITE : VGuiConstants.COLOR_GRAY;
        GuiRenderer.fill(this.x, this.y, this.x + this.width, this.y + this.height, backgroundColor);
        this.renderHovering(0, 0);
        if (isFocused && this.cursorPos != this.selectionEnd) {
            int start = Math.min(this.cursorPos, this.selectionEnd);
            int end = Math.max(this.cursorPos, this.selectionEnd);
            String before = this.text.substring(0, start);
            String selected = this.text.substring(start, end);
            int xBefore = this.x + 8 + class_310.method_1551().field_1772.method_1727(before);
            int xSelected = class_310.method_1551().field_1772.method_1727(selected);
            int selColor = ColorUtil.ARGB.multiplyAlpha(VGuiConstants.COLOR_RED, 0.55f);
            GuiRenderer.fill(xBefore, this.y + 4, xBefore + xSelected, this.y + this.height - 4, selColor);
        }
        class_2561 displayText = hasText ? class_2561.method_43470((String)this.text) : this.placeholder;
        GuiRenderer.drawString(class_310.method_1551().field_1772, displayText, this.x + 8, this.y + (this.height - 8) / 2, textColor | 0xFF000000);
        if (isFocused && this.showCursor) {
            String beforeCursor = this.text.substring(0, this.cursorPos);
            int cursorX = this.x + 8 + class_310.method_1551().field_1772.method_1727(beforeCursor);
            GuiRenderer.fill(cursorX, this.y + 6, cursorX + 1, this.y + this.height - 6, VGuiConstants.COLOR_WHITE);
        }
        if (isFocused) {
            int borderColor = ColorUtil.ARGB.multiplyAlpha(VGuiConstants.COLOR_RED, 0.8f);
            GuiRenderer.renderBorder(this.x, this.y, this.x + this.width, this.y + this.height, 1, borderColor);
        }
        if (isFocused) {
            long time = class_156.method_658();
            if (time - this.lastBlinkTime > 500L) {
                this.showCursor = !this.showCursor;
                this.lastBlinkTime = time;
            }
        } else {
            this.showCursor = true;
        }
    }

    public boolean method_25404(class_11908 keyEvent) {
        if (!this.focused && !this.selected) {
            return false;
        }
        boolean shift = keyEvent.method_74239();
        boolean ctrl = keyEvent.method_74240();
        if (keyEvent.comp_4795() == 257 || keyEvent.comp_4795() == 335) {
            this.onSearch.accept(this);
            return true;
        }
        if (this.cursorPos != this.selectionEnd) {
            int start = Math.min(this.cursorPos, this.selectionEnd);
            int end = Math.max(this.cursorPos, this.selectionEnd);
            if (keyEvent.comp_4795() == 259 || keyEvent.comp_4795() == 261) {
                this.text = this.text.substring(0, start) + this.text.substring(end);
                this.cursorPos = start;
                this.selectionEnd = start;
                this.onSearch.accept(this);
                return true;
            }
        }
        if (keyEvent.comp_4795() == 259) {
            if (this.cursorPos > 0) {
                this.text = this.text.substring(0, this.cursorPos - 1) + this.text.substring(this.cursorPos);
                --this.cursorPos;
                this.selectionEnd = this.cursorPos;
                this.onSearch.accept(this);
            }
            return true;
        }
        if (keyEvent.comp_4795() == 261) {
            if (this.cursorPos < this.text.length()) {
                this.text = this.text.substring(0, this.cursorPos) + this.text.substring(this.cursorPos + 1);
                this.onSearch.accept(this);
            }
            return true;
        }
        if (ctrl && keyEvent.comp_4795() == 65) {
            this.cursorPos = this.text.length();
            this.selectionEnd = 0;
            return true;
        }
        if (keyEvent.comp_4795() == 263) {
            if (this.cursorPos > 0) {
                --this.cursorPos;
            }
            if (!shift) {
                this.selectionEnd = this.cursorPos;
            }
            return true;
        }
        if (keyEvent.comp_4795() == 262) {
            if (this.cursorPos < this.text.length()) {
                ++this.cursorPos;
            }
            if (!shift) {
                this.selectionEnd = this.cursorPos;
            }
            return true;
        }
        String keyName = GLFW.glfwGetKeyName((int)keyEvent.comp_4795(), (int)keyEvent.comp_4796());
        if (keyName != null && keyName.length() == 1) {
            char c;
            char c2 = c = keyEvent.method_74239() ? keyName.toUpperCase().charAt(0) : keyName.charAt(0);
            if (this.cursorPos != this.selectionEnd) {
                int start = Math.min(this.cursorPos, this.selectionEnd);
                int end = Math.max(this.cursorPos, this.selectionEnd);
                this.text = this.text.substring(0, start) + c + this.text.substring(end);
                this.cursorPos = start + 1;
            } else {
                this.text = this.text.substring(0, this.cursorPos) + c + this.text.substring(this.cursorPos);
                ++this.cursorPos;
            }
            this.selectionEnd = this.cursorPos;
            this.onSearch.accept(this);
            return true;
        }
        return false;
    }

    public String getInput() {
        return this.text;
    }

    public void setInput(String input) {
        this.text = input != null ? input : "";
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

    @Override
    public boolean method_25402(class_11909 event, boolean bl) {
        if (!this.active || !this.visible) {
            return false;
        }
        boolean clicked = this.clicked(event.comp_4798(), event.comp_4799());
        if (clicked) {
            this.method_25365(true);
            this.selected = true;
            int relX = (int)event.comp_4798() - (this.x + 8);
            int pos = 0;
            for (int i = 0; i < this.text.length() && class_310.method_1551().field_1772.method_1727(this.text.substring(0, i + 1)) <= relX; ++i) {
                pos = i + 1;
            }
            this.cursorPos = pos;
            this.selectionEnd = pos;
            return true;
        }
        this.method_25365(false);
        this.selected = false;
        return false;
    }

    @Override
    public void method_25365(boolean focused) {
        super.method_25365(focused);
        if (!focused) {
            this.selected = false;
        }
    }
}

