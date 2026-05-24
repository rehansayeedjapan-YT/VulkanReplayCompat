/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 */
package net.vulkanmod.config.option;

import net.minecraft.class_124;
import net.minecraft.class_2561;

public enum PerformanceImpact {
    LOW((class_2561)class_2561.method_43471((String)"vulkanmod.options.performanceImpact.low").method_27692(class_124.field_1077)),
    MEDIUM((class_2561)class_2561.method_43471((String)"vulkanmod.options.performanceImpact.medium").method_27692(class_124.field_1054)),
    HIGH((class_2561)class_2561.method_43471((String)"vulkanmod.options.performanceImpact.high").method_27692(class_124.field_1061));

    private final class_2561 component;

    private PerformanceImpact(class_2561 component) {
        this.component = component;
    }

    public class_2561 component() {
        return this.component;
    }
}

