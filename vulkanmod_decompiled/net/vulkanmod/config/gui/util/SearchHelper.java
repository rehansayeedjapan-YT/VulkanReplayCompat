/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 */
package net.vulkanmod.config.gui.util;

import java.util.function.Function;
import net.minecraft.class_2561;
import net.vulkanmod.config.option.CyclingOption;

public class SearchHelper {
    public static <T> boolean matchesAnyValue(CyclingOption<T> cycling, String searchTerm) {
        Function<T, class_2561> translator = cycling.getTranslator();
        for (T value : cycling.getValues()) {
            String translated = translator.apply(value).getString().toLowerCase();
            if (!translated.contains(searchTerm)) continue;
            return true;
        }
        return false;
    }
}

