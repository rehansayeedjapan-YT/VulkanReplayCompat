/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_124
 *  net.minecraft.class_156
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_364
 *  net.minecraft.class_437
 *  net.minecraft.class_446
 *  net.minecraft.class_5244
 *  net.minecraft.class_5348
 *  net.minecraft.class_5481
 */
package net.vulkanmod.config.gui;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_124;
import net.minecraft.class_156;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_437;
import net.minecraft.class_446;
import net.minecraft.class_5244;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import net.vulkanmod.config.UpdateChecker;
import net.vulkanmod.config.gui.ModSettingsEntry;
import net.vulkanmod.config.gui.ModSettingsRegistry;
import net.vulkanmod.config.gui.OptionBlock;
import net.vulkanmod.config.gui.VOptionList;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.config.gui.util.SearchHelper;
import net.vulkanmod.config.gui.util.VGuiConstants;
import net.vulkanmod.config.gui.widget.ModIconWidget;
import net.vulkanmod.config.gui.widget.VAbstractWidget;
import net.vulkanmod.config.gui.widget.VButtonWidget;
import net.vulkanmod.config.gui.widget.VTextInputWidget;
import net.vulkanmod.config.option.CyclingOption;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.option.OptionPage;
import net.vulkanmod.config.option.PerformanceImpact;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.util.ColorUtil;

public class VOptionScreen
extends class_437 {
    public static final int MARGIN = 10;
    private final class_437 parent;
    private final List<ModSettingsEntry> modSettingsEntries;
    private final List<OptionPage> optionPages;
    private OptionPage searchResultsPage;
    private int currentListIdx = 0;
    private boolean isSearchActive = false;
    private int tooltipX;
    private int tooltipY;
    private int tooltipWidth;
    private VButtonWidget applyButton;
    private VButtonWidget undoButton;
    private VTextInputWidget searchField;
    private final List<VAbstractWidget> iconWidgets = Lists.newArrayList();
    private final List<VButtonWidget> pageButtons = Lists.newArrayList();
    private final List<VButtonWidget> buttons = Lists.newArrayList();

    public VOptionScreen(class_2561 title, class_437 parent) {
        super(title);
        this.parent = parent;
        this.optionPages = new ArrayList<OptionPage>();
        this.modSettingsEntries = new ArrayList<ModSettingsEntry>(ModSettingsRegistry.INSTANCE.getModEntries());
    }

    protected void method_25426() {
        this.initOptionsPages();
        if (this.optionPages.isEmpty()) {
            throw new IllegalStateException("Default Options weren't added!");
        }
        int top = 32;
        int bottom = 60;
        int itemHeight = 20;
        int leftMargin = 116;
        int listWidth = Math.min(this.field_22789 - leftMargin - 10, 420);
        int listHeight = this.field_22790 - top - bottom;
        this.buildLists(leftMargin, top, listWidth, listHeight, itemHeight);
        this.searchField = this.createSearchField();
        int x = leftMargin + listWidth + 6;
        int tooltipWidth = Math.min(this.field_22789 - x - 10, 420);
        int y = top + itemHeight + 6;
        if (tooltipWidth < 200) {
            x = leftMargin + 3;
            tooltipWidth = listWidth;
            y = this.field_22790 - bottom + 10;
        }
        this.tooltipX = x;
        this.tooltipY = y;
        this.tooltipWidth = tooltipWidth;
        this.buildPage();
        this.applyButton.active = false;
        this.undoButton.visible = false;
    }

    private void initOptionsPages() {
        this.optionPages.clear();
        for (ModSettingsEntry modPageSet : this.modSettingsEntries) {
            modPageSet.initPages();
            this.optionPages.addAll(modPageSet.getPages());
        }
    }

    private VTextInputWidget createSearchField() {
        int rightMargin = 10;
        int padding = 10;
        int kofiWidth = class_310.method_1551().field_1772.method_27525((class_5348)class_2561.method_43471((String)"vulkanmod.options.buttons.kofi")) + padding;
        int topBarRight = this.field_22789 - kofiWidth - rightMargin;
        if (UpdateChecker.isUpdateAvailable()) {
            int updateWidth = this.field_22787.field_1772.method_27525((class_5348)class_2561.method_43471((String)"vulkanmod.options.buttons.update_available")) + padding;
            topBarRight -= updateWidth + 5;
        }
        int leftMargin = 116;
        int width = Math.min(topBarRight - leftMargin - 4, 413);
        return new VTextInputWidget(leftMargin, 4, width, 20, (class_2561)class_2561.method_43471((String)"vulkanmod.options.searchFieldPlaceholder"), widget -> this.performSearch(widget.getInput()));
    }

    private void buildLists(int left, int top, int listWidth, int listHeight, int itemHeight) {
        for (OptionPage page : this.optionPages) {
            page.createList(left, top, listWidth, listHeight, itemHeight);
            page.updateOptionStates();
        }
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            this.isSearchActive = false;
            this.currentListIdx = 0;
            this.buildPage();
            return;
        }
        String searchTerm = query.toLowerCase().trim();
        ArrayList<OptionBlock> searchResults = new ArrayList<OptionBlock>();
        for (OptionPage page : this.optionPages) {
            ArrayList matchingOptions = new ArrayList();
            for (OptionBlock block : page.optionBlocks) {
                for (Option<?> option : block.options()) {
                    CyclingOption cycling;
                    boolean matches = false;
                    String optionName = option.getName().getString().toLowerCase();
                    String optionTooltip = option.getTooltip() != null ? option.getTooltip().getString().toLowerCase() : "";
                    String displayedValue = option.getDisplayedValue().getString().toLowerCase();
                    if (optionName.contains(searchTerm) || optionTooltip.contains(searchTerm) || displayedValue.contains(searchTerm)) {
                        matches = true;
                    } else if (option instanceof CyclingOption && SearchHelper.matchesAnyValue(cycling = (CyclingOption)option, searchTerm)) {
                        matches = true;
                    }
                    if (!matches) continue;
                    matchingOptions.add(option);
                }
            }
            if (matchingOptions.isEmpty()) continue;
            searchResults.add(new OptionBlock("\u00a7l" + page.name, matchingOptions.toArray(new Option[0])));
            searchResults.add(new OptionBlock("", new Option[0]));
        }
        this.searchResultsPage = new OptionPage("Search Results", searchResults.toArray(new OptionBlock[0]));
        int top = 32;
        int bottom = 60;
        int itemHeight = 20;
        int leftMargin = 116;
        int listWidth = Math.min(this.field_22789 - leftMargin - 10, 420);
        int listHeight = this.field_22790 - top - bottom;
        this.searchResultsPage.createList(leftMargin, top, listWidth, listHeight, itemHeight);
        this.isSearchActive = true;
        this.buildPage();
    }

    private void buildPage() {
        this.buttons.clear();
        this.pageButtons.clear();
        this.iconWidgets.clear();
        String savedInput = this.searchField != null ? this.searchField.getInput() : "";
        boolean savedFocused = this.searchField != null && this.searchField.focused;
        boolean savedSelected = this.searchField != null && this.searchField.selected;
        this.method_37067();
        int x = 10;
        int y = 4;
        int width = 100;
        int j = 0;
        for (ModSettingsEntry modEntry : this.modSettingsEntries) {
            ModIconWidget iconWidget = new ModIconWidget(modEntry.modName, modEntry.getIcon(), x, y, width, 28);
            this.iconWidgets.add(iconWidget);
            this.method_25429(iconWidget);
            y += 28;
            List<OptionPage> pages = modEntry.getPages();
            for (OptionPage page : pages) {
                int finalIdx = j++;
                VButtonWidget widget = new VButtonWidget(x, y, width, 20, class_2561.method_30163((String)page.name), button -> this.setOptionList(finalIdx));
                widget.setTextLayout(false, 12);
                this.buttons.add(widget);
                this.pageButtons.add(widget);
                this.method_25429(widget);
                y += 20;
            }
        }
        if (!this.isSearchActive) {
            this.pageButtons.get(this.currentListIdx).setSelected(true);
            VOptionList currentList = this.optionPages.get(this.currentListIdx).getOptionList();
            this.method_25429(currentList);
        } else if (this.searchResultsPage != null) {
            VOptionList searchList = this.searchResultsPage.getOptionList();
            this.method_25429(searchList);
            this.searchResultsPage.updateOptionStates();
        }
        this.addButtonsWithSearchBar();
        this.searchField.setInput(savedInput);
        if (savedFocused) {
            this.searchField.method_25365(true);
            this.searchField.setSelected(savedSelected);
        }
    }

    private void addButtonsWithSearchBar() {
        int rightMargin = 10;
        int padding = 10;
        int buttonWidth = class_310.method_1551().field_1772.method_27525((class_5348)class_5244.field_24334) + 2 * padding;
        int x0 = this.field_22789 - buttonWidth - rightMargin;
        int y0 = this.field_22790 - 20 - 7;
        VButtonWidget doneButton = new VButtonWidget(x0, y0, buttonWidth, 20, class_5244.field_24334, button -> class_310.method_1551().method_1507(this.parent));
        buttonWidth = class_310.method_1551().field_1772.method_27525((class_5348)class_2561.method_43471((String)"vulkanmod.options.buttons.apply")) + 2 * padding;
        this.applyButton = new VButtonWidget(x0 -= buttonWidth + 5, y0, buttonWidth, 20, (class_2561)class_2561.method_43471((String)"vulkanmod.options.buttons.apply"), button -> this.applyOptions());
        buttonWidth = class_310.method_1551().field_1772.method_27525((class_5348)class_2561.method_43471((String)"vulkanmod.options.buttons.undo")) + 2 * padding;
        this.undoButton = new VButtonWidget(x0 -= buttonWidth + 5, y0, buttonWidth, 20, (class_2561)class_2561.method_43471((String)"vulkanmod.options.buttons.undo"), button -> this.undo());
        int kofiWidth = class_310.method_1551().field_1772.method_27525((class_5348)class_2561.method_43471((String)"vulkanmod.options.buttons.kofi")) + padding;
        int kofiX = this.field_22789 - kofiWidth - rightMargin;
        VButtonWidget supportButton = new VButtonWidget(kofiX, 4, kofiWidth, 20, (class_2561)class_2561.method_43471((String)"vulkanmod.options.buttons.kofi"), button -> class_156.method_668().method_670("https://ko-fi.com/xcollateral"));
        this.buttons.add(this.applyButton);
        this.buttons.add(doneButton);
        this.buttons.add(supportButton);
        this.buttons.add(this.undoButton);
        this.method_25429(this.applyButton);
        this.method_25429(doneButton);
        this.method_25429(supportButton);
        this.method_25429(this.undoButton);
        this.method_25429(this.searchField);
        if (UpdateChecker.isUpdateAvailable()) {
            assert (this.field_22787 != null);
            int updateWidth = this.field_22787.field_1772.method_27525((class_5348)class_2561.method_43471((String)"vulkanmod.options.buttons.update_available")) + padding;
            VButtonWidget updateButton = new VButtonWidget(kofiX - updateWidth - 5, 4, updateWidth, 20, (class_2561)class_2561.method_43471((String)"vulkanmod.options.buttons.update_available").method_27692(class_124.field_1073), button -> class_156.method_668().method_670("https://modrinth.com/mod/vulkanmod"));
            this.buttons.add(updateButton);
            this.method_25429(updateButton);
        }
    }

    public boolean method_25402(class_11909 event, boolean bl) {
        for (class_364 element : this.method_25396()) {
            if (!element.method_25402(event, bl)) continue;
            this.method_25395(element);
            if (event.method_74245() == 0) {
                this.method_25398(true);
            }
            this.updateState();
            return true;
        }
        return false;
    }

    public boolean method_25406(class_11909 event) {
        this.method_25398(false);
        this.updateState();
        return this.method_19355(event.comp_4798(), event.comp_4799()).filter(guiEventListener -> guiEventListener.method_25406(event)).isPresent();
    }

    public void method_25419() {
        class_310.method_1551().method_1507(this.parent);
    }

    public void method_25394(class_332 guiGraphics, int mouseX, int mouseY, float delta) {
        GuiRenderer.guiGraphics = guiGraphics;
        VRenderSystem.enableBlend();
        VOptionList currentList = this.isSearchActive && this.searchResultsPage != null ? this.searchResultsPage.getOptionList() : this.optionPages.get(this.currentListIdx).getOptionList();
        currentList.updateState(mouseX, mouseY);
        currentList.renderWidget(mouseX, mouseY);
        for (VAbstractWidget widget : this.iconWidgets) {
            widget.render(mouseX, mouseY);
        }
        for (VButtonWidget button : this.buttons) {
            button.updateState(mouseX, mouseY);
            button.render(mouseX, mouseY);
        }
        this.searchField.updateState(mouseX, mouseY);
        this.searchField.render(mouseX, mouseY);
        VAbstractWidget hoveredWidget = null;
        for (VButtonWidget b : this.buttons) {
            if (!b.method_25405(mouseX, mouseY)) continue;
            hoveredWidget = b;
            break;
        }
        if (hoveredWidget == null) {
            hoveredWidget = currentList.getHoveredWidget(mouseX, mouseY);
        }
        if (hoveredWidget != null) {
            this.renderTooltip(hoveredWidget, this.tooltipX, this.tooltipY);
        }
    }

    private void renderTooltip(VAbstractWidget widget, int x, int y) {
        List<class_5481> list = this.getWidgetTooltip(widget);
        if (list.isEmpty()) {
            return;
        }
        int lines = list.size();
        int padding = 3;
        int width = GuiRenderer.getMaxTextWidth(this.field_22793, list);
        int height = lines * 10;
        float intensity = 0.05f;
        int color = ColorUtil.ARGB.pack(intensity, intensity, intensity, 0.6f);
        GuiRenderer.fill(x - padding, y - padding, x + width + padding, y + height + padding, color);
        color = VGuiConstants.COLOR_RED;
        GuiRenderer.renderBorder(x - padding, y - padding, x + width + padding, y + height + padding, 1, color);
        int yOffset = 0;
        for (class_5481 text : list) {
            GuiRenderer.drawString(this.field_22793, text, x, y + yOffset, -1);
            yOffset += 10;
        }
    }

    private List<class_5481> getWidgetTooltip(VAbstractWidget widget) {
        class_2561 tooltip = widget.getTooltip();
        PerformanceImpact impact = widget.getImpact();
        ArrayList<class_5481> textList = new ArrayList<class_5481>();
        if (tooltip != null) {
            textList.addAll(this.field_22793.method_1728((class_5348)tooltip, this.tooltipWidth));
        }
        if (impact != null) {
            textList.addAll(this.field_22793.method_1728((class_5348)class_2561.method_43469((String)"Performance Impact: %s", (Object[])new Object[]{impact.component()}), this.tooltipWidth));
        }
        return textList;
    }

    private void updateState() {
        if (this.applyButton == null | this.undoButton == null) {
            return;
        }
        boolean modified = false;
        for (OptionPage page : this.optionPages) {
            modified |= page.optionChanged();
        }
        if (modified) {
            for (OptionPage page : this.optionPages) {
                page.optionChanged();
            }
        }
        this.applyButton.active = modified;
        this.undoButton.visible = modified;
    }

    private void setOptionList(int i) {
        this.currentListIdx = i;
        this.isSearchActive = false;
        this.searchField.setInput("");
        this.searchField.method_25365(false);
        this.buildPage();
        this.pageButtons.get(i).setSelected(true);
    }

    private void undo() {
        for (OptionPage page : this.optionPages) {
            page.resetToOriginalState();
            page.updateOptionStates();
        }
        this.buildPage();
    }

    private void applyOptions() {
        List<OptionPage> pages = List.copyOf(this.optionPages);
        for (OptionPage page : pages) {
            page.applyOptionChanges();
            page.updateOptionStates();
        }
        for (ModSettingsEntry modEntry : this.modSettingsEntries) {
            modEntry.runOnApply();
        }
    }

    public boolean method_25404(class_11908 keyEvent) {
        if (keyEvent.method_74240() && keyEvent.comp_4795() == 76) {
            this.method_25395(this.searchField);
            this.searchField.method_25365(true);
            this.searchField.setSelected(true);
            return true;
        }
        if (keyEvent.comp_4795() == 256 && this.isSearchActive) {
            this.isSearchActive = false;
            this.searchField.setInput("");
            this.searchField.method_25365(false);
            this.buildPage();
            this.pageButtons.get(this.currentListIdx).setSelected(true);
            return true;
        }
        if (!this.searchField.focused && keyEvent.comp_4795() == 80 && keyEvent.method_74239()) {
            class_310.method_1551().method_1507((class_437)new class_446((class_437)this, class_310.method_1551(), class_310.method_1551().field_1690));
            return false;
        }
        return super.method_25404(keyEvent);
    }
}

