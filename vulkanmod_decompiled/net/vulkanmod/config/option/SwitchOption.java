/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 */
package net.vulkanmod.config.option;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.vulkanmod.config.gui.widget.OptionWidget;
import net.vulkanmod.config.gui.widget.SwitchOptionWidget;
import net.vulkanmod.config.option.Option;

public class SwitchOption
extends Option<Boolean> {
    public SwitchOption(class_2561 name, Consumer<Boolean> setter, Supplier<Boolean> getter) {
        super(name, setter, getter, i -> class_2561.method_30163((String)String.valueOf(i)));
    }

    @Override
    protected OptionWidget<?> createWidget() {
        SwitchOptionWidget widget = new SwitchOptionWidget(this, this.name);
        this.widget = widget;
        return widget;
    }
}

