/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_5348
 */
package net.vulkanmod.config.gui;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.Set;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_5348;
import net.vulkanmod.Initializer;
import net.vulkanmod.config.gui.ModSettingsEntry;
import net.vulkanmod.config.option.Options;

public class ModSettingsRegistry {
    public static final ModSettingsRegistry INSTANCE = new ModSettingsRegistry();
    private final Set<ModSettingsEntry> modEntries = new ObjectArraySet();

    ModSettingsRegistry() {
        ModSettingsEntry vulkanModSettings = new ModSettingsEntry((class_5348)class_2561.method_43470((String)"VulkanMod").method_27692(class_124.field_1079), () -> class_2960.method_60655((String)"vulkanmod", (String)"vlogo_transparent.png"), Options::getOptionPages, () -> Initializer.CONFIG.write());
        this.addModEntry(vulkanModSettings);
    }

    public void addModEntry(ModSettingsEntry entry) {
        this.modEntries.add(entry);
    }

    public Set<ModSettingsEntry> getModEntries() {
        return this.modEntries;
    }
}

