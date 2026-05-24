/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2960
 *  net.minecraft.class_5348
 */
package net.vulkanmod.config.gui;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.class_2960;
import net.minecraft.class_5348;
import net.vulkanmod.config.option.OptionPage;

public class ModSettingsEntry {
    public final class_5348 modName;
    public final Supplier<class_2960> iconSupplier;
    private final Supplier<List<OptionPage>> optionPageSupplier;
    private final Runnable onApply;
    private class_2960 icon;
    List<OptionPage> pages;

    public ModSettingsEntry(class_5348 modName, Supplier<class_2960> iconSupplier, Supplier<List<OptionPage>> optionPageSupplier, Runnable onApply) {
        this.modName = modName;
        this.iconSupplier = iconSupplier;
        this.optionPageSupplier = optionPageSupplier;
        this.onApply = onApply;
    }

    public List<OptionPage> initPages() {
        this.pages = this.optionPageSupplier.get();
        return this.pages;
    }

    public List<OptionPage> getPages() {
        return this.pages;
    }

    public class_2960 getIcon() {
        if (this.icon == null) {
            this.icon = this.iconSupplier.get();
        }
        return this.icon;
    }

    public void runOnApply() {
        this.onApply.run();
    }
}

